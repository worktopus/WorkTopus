// [manage-member.js : 팀원 역할 렌더링 및 비동기 직급 변경/추방 제어]
document.addEventListener('DOMContentLoaded', function () {
    const container = document.querySelector('.members-table-wrapper table tbody') || document.querySelector('#member-tab table tbody');

    // CSRF 보안 토큰 파싱
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

    // URL 구조 분석하여 workspaceId 바인딩
    const pathSegments = window.location.pathname.split('/').filter(Boolean);
    let workspaceId = 22;

    const membersIndex = pathSegments.indexOf('members');
    if (membersIndex > 0) {
        workspaceId = pathSegments[membersIndex - 1];
    } else {
        workspaceId = pathSegments[pathSegments.length - 1] || 22;
    }

    // 기능 1 : DB 백엔드로부터 실시간 팀원 목록 JSON 수집
    function loadWorkspaceMembers() {
        fetch(`/api/manage/${workspaceId}/members-data`, { method: 'GET' })
            .then(response => {
                if (response.ok) return response.json();
                throw new Error('팀원 목록을 불러오는 데 실패했습니다.');
            })
            .then(members => {
                console.log("백엔드에서 넘어온 데이터 전체보기:", members);
                renderMemberList(members);
            })
            .catch(error => { console.error('Fetch Members Error:', error); });
    }

    // 기능 2 : 수집된 DB 인자로 팀원 테이블 마크업 동적 드로잉 (현재 직급 삭제 반영 완료)
    function renderMemberList(members) {
        if (!container) return;
        container.innerHTML = '';

        // [보완] 바인딩된 목록이 없을 때 새 헤더 규격에 맞춰 colspan=4 지정
        if (!members || members.length === 0) {
            container.innerHTML = '<tr><td colspan="4" style="padding: 40px; text-align: center; color: var(--text-sub);">현재 참여 중인 팀원이 없습니다.</td></tr>';
            return;
        }

        members.forEach(member => {
            const tr = document.createElement('tr');
            tr.className = 'member-row';
            tr.style.borderBottom = '1px solid var(--border)';
            tr.setAttribute('data-member-id', member.id);

            // [수정] 오라클 DB의 ROLE 값(OWNER, MEMBER) 또는 백엔드 규격(LEADER)을 통합 지원
            const currentRole = member.projectRole || member.role;

            // OWNER 혹은 LEADER일 때 모두 '팀장'으로 매핑
            const isLeader = (currentRole === 'OWNER' || currentRole === 'LEADER') ? 'selected' : '';
            const isSubLeader = (currentRole === 'SUB_LEADER') ? 'selected' : '';
            // MEMBER일 때 '팀원'으로 매핑
            const isMember = (currentRole === 'MEMBER') ? 'selected' : '';

            // [수정/삭제] 기존 현재 직급 배지 td 열을 제거하고 4개 컬럼 체계로 재편성
            // [수정] 담당 역할 칸에 이전에 드린 HTML 규격에 맞춰 '저장 버튼' 동적 생성 추가
            tr.innerHTML = `
                <td style="padding: 16px;">
                    <div style="font-weight: 600; color: var(--text-main);">${member.userName || '이름 없음'}</div>
                    <div style="font-size: 0.8rem; color: var(--text-sub);">${member.userEmail || '이메일 없음'}</div>
                </td>
                <td style="padding: 16px;">
                    <!-- [수정] OWNER 또는 LEADER 권한일 때 셀렉트 박스 비활성화 조건 처리 -->
                    <select class="form-input role-select" data-id="${member.id}" ${(currentRole === 'OWNER' || currentRole === 'LEADER') ? 'disabled' : ''} style="padding: 6px 12px; font-size: 0.85rem; border: 1px solid var(--border); border-radius: 4px;">
                        <option value="LEADER" ${isLeader}>팀장</option>
                        <option value="SUB_LEADER" ${isSubLeader}>부팀장</option>
                        <option value="MEMBER" ${isMember}>팀원</option>
                    </select>
                </td>
                <td style="padding: 16px;">
                    <div style="display: flex; gap: 6px; align-items: center;">
                        <input type="text" id="task-input-${member.id}" class="form-input task-input" data-id="${member.id}" value="${member.assignedRole || ''}" placeholder="예: UI 디자인, QA" style="padding: 6px 12px; font-size: 0.85rem; width: 70%; border: 1px solid var(--border); border-radius: 4px;">
                        <button class="btn btn-primary btn-save-task" data-id="${member.id}" style="padding: 6px 10px; font-size: 0.8rem; cursor: pointer; background: var(--primary, #4a90e2); color: #fff; border: none; border-radius: 4px;">저장</button>
                    </div>
                </td>
                <td style="padding: 16px; text-align: center;">
                    <!-- [수정] OWNER 또는 LEADER 권한일 때 제외 버튼 숨김 처리 -->
                    <button class="btn btn-danger-outline kick-btn" data-id="${member.id}" ${(currentRole === 'OWNER' || currentRole === 'LEADER') ? 'style="display:none;"' : ''} style="padding: 6px 12px; font-size: 0.8rem; cursor: pointer; background: transparent; color: var(--danger); border: 1px solid var(--danger); border-radius: 4px;">제외</button>
                </td>
            `;
            container.appendChild(tr);
        });
    }
    if (container) {
        // 기능 3 : 직급 셀렉트 체인지 비동기 수정 통신
        container.addEventListener('change', function (e) {
            if (e.target && e.target.classList.contains('role-select')) {
                const selectBox = e.target;
                const memberId = selectBox.getAttribute('data-id');
                const selectedRole = selectBox.value;

                fetch('/api/manage/member/role-update', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', [csrfHeader]: csrfToken },
                    body: JSON.stringify({ memberId: parseInt(memberId), projectRole: selectedRole })
                })
                    .then(response => {
                        if (response.ok) return response.json();
                        return response.json().then(err => { throw new Error(err.error); });
                    })
                    .then(data => {
                        alert(data.message || '직급이 변경되었습니다.');
                    })
                    .catch(error => { alert('직급 변경 실패: ' + error.message); });
            }
        });

        // 기능 4 : 팀원 공간 강제 제외(추방) 및 담당 역할 개별 저장 버튼 클릭 비동기 통신
        container.addEventListener('click', function (e) {
            // [추방 버튼 처리]
            if (e.target && e.target.classList.contains('kick-btn')) {
                const kickBtn = e.target;
                const memberId = kickBtn.getAttribute('data-id');
                const targetRow = kickBtn.closest('tr');

                if (confirm('정말로 이 팀원을 현재 워크스페이스 공간에서 강제 제외하시겠습니까?')) {
                    fetch(`/api/manage/member/${memberId}`, { method: 'DELETE', headers: { [csrfHeader]: csrfToken } })
                        .then(response => {
                            if (response.ok) return response.json();
                            return response.json().then(err => { throw new Error(err.error); });
                        })
                        .then(data => {
                            alert(data.message || '팀원이 제외되었습니다.');
                            if (targetRow) targetRow.remove();
                        })
                        .catch(error => { alert('제외 처리 실패: ' + error.message); });
                }
            }

            // [새로 추가됨] 담당 역할 저장 버튼 클릭 처리 핸들러
            if (e.target && e.target.classList.contains('btn-save-task')) {
                const saveBtn = e.target;
                const memberId = saveBtn.getAttribute('data-id');
                const inputField = document.getElementById(`task-input-${memberId}`);

                if (inputField) {
                    const assignedRoleValue = inputField.value.trim();

                    fetch('/api/manage/member/task-update', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json', [csrfHeader]: csrfToken },
                        body: JSON.stringify({ memberId: parseInt(memberId), assignedRole: assignedRoleValue })
                    })
                        .then(response => {
                            if (response.ok) {
                                alert('담당 역할이 오라클 DB에 성공적으로 저장되었습니다.');

                                // 성공 시 인풋창 배경 피드백 시각 효과
                                inputField.style.backgroundColor = '#f6ffed';
                                setTimeout(() => { inputField.style.backgroundColor = '#fff'; }, 500);

                                // 상단 헤더 프로필 이름과 비교하여 즉시 동기화
                                const currentRow = saveBtn.closest('tr');
                                const rowUserName = currentRow.querySelector('td:first-child div:first-child')?.textContent.trim();
                                const headerUserName = document.querySelector('#header-user-name')?.textContent.trim();

                                if (headerUserName && rowUserName === headerUserName) {
                                    const headerUserTask = document.querySelector('#header-user-task');
                                    if (headerUserTask) {
                                        headerUserTask.textContent = assignedRoleValue || 'Backend Developer';
                                    }
                                }
                                return response.json();
                            }
                            throw new Error('역할 저장 실패');
                        })
                        .catch(error => {
                            alert('역할 저장 중 오류가 발생했습니다.');
                            console.error('Save Button Error:', error);
                        });
                }
            }
        });

        // 기능 5 : 담당 역할(task-input) 입력 후 포커스가 벗어나면(blur) 실시간 비동기 자동 저장 + 헤더 즉시 동기화
        container.addEventListener('focusout', function (e) {
            if (e.target && e.target.classList.contains('task-input')) {
                const inputField = e.target;
                const memberId = inputField.getAttribute('data-id');
                const assignedRoleValue = inputField.value.trim();

                fetch('/api/manage/member/task-update', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', [csrfHeader]: csrfToken },
                    body: JSON.stringify({ memberId: parseInt(memberId), assignedRole: assignedRoleValue })
                })
                    .then(response => {
                        if (response.ok) {
                            // 성공 시 인풋창 배경 피드백 시각 효과 (초록빛 맴돌다 사라짐)
                            inputField.style.backgroundColor = '#f6ffed';
                            setTimeout(() => { inputField.style.backgroundColor = '#fff'; }, 500);

                            // =========================================================
                            // [교정] 이메일을 제외한 순수 유저 '이름'만 정밀 추출하여 헤더와 비교
                            // =========================================================
                            const currentRow = inputField.closest('tr');
                            const rowUserName = currentRow.querySelector('td:first-child div:first-child')?.textContent.trim();
                            const headerUserName = document.querySelector('#header-user-name')?.textContent.trim();

                            console.log("화면 행 이름:", rowUserName, " | 헤더 이름:", headerUserName);

                            if (headerUserName && rowUserName === headerUserName) {
                                const headerUserTask = document.querySelector('#header-user-task');
                                if (headerUserTask) {
                                    headerUserTask.textContent = assignedRoleValue || 'Backend Developer';
                                }
                            }

                            return response.json();
                        }
                        throw new Error('역할 자동 저장 실패');
                    })
                    .catch(error => { console.error('Auto Save Error:', error); });
            }
        });

    }

    // 파일 로드 시 실시간 데이터 호출 가동
    loadWorkspaceMembers();
});
