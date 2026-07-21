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
            .then(members => { renderMemberList(members); })
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

            const isLeader = member.projectRole === 'LEADER' ? 'selected' : '';
            const isSubLeader = member.projectRole === 'SUB_LEADER' ? 'selected' : '';
            const isMember = member.projectRole === 'MEMBER' ? 'selected' : '';

            // [수정/삭제] 기존 현재 직급 배지 td 열을 제거하고 4개 컬럼 체계로 재편성
            tr.innerHTML = `
                <td style="padding: 16px;">
                    <div style="font-weight: 600; color: var(--text-main);">${member.userName || '이름 없음'}</div>
                    <div style="font-size: 0.8rem; color: var(--text-sub);">${member.userEmail || '이메일 없음'}</div>
                </td>
                <td style="padding: 16px;">
                    <select class="form-input role-select" data-id="${member.id}" ${member.projectRole === 'LEADER' ? 'disabled' : ''} style="padding: 6px 12px; font-size: 0.85rem; border: 1px solid var(--border); border-radius: 4px;">
                        <option value="LEADER" ${isLeader}>팀장</option>
                        <option value="SUB_LEADER" ${isSubLeader}>부팀장</option>
                        <option value="MEMBER" ${isMember}>팀원</option>
                    </select>
                </td>
                <td style="padding: 16px;">
                    <input type="text" class="form-input task-input" data-id="${member.id}" value="${member.assignedRole || ''}" placeholder="예: UI 디자인, QA" style="padding: 6px 12px; font-size: 0.85rem; width: 80%; border: 1px solid var(--border); border-radius: 4px;">
                </td>
                <td style="padding: 16px; text-align: center;">
                    <button class="btn btn-danger-outline kick-btn" data-id="${member.id}" ${member.projectRole === 'LEADER' ? 'style="display:none;"' : ''} style="padding: 6px 12px; font-size: 0.8rem; cursor: pointer; background: transparent; color: var(--danger); border: 1px solid var(--danger); border-radius: 4px;">제외</button>
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
                        // [참고] 배지 열이 삭제되었으므로 배지 텍스트 갱신 구문 생략 처리 자동 마감
                    })
                    .catch(error => { alert('직급 변경 실패: ' + error.message); });
            }
        });

        // 기능 4 : 팀원 공간 강제 제외(추방) 비동기 통신
        container.addEventListener('click', function (e) {
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
        });

        // 기능 5 : 담당 역할(task-input) 입력 후 포커스가 벗어나면(blur) 실시간 비동기 자동 저장
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
                            // 성공 시 유저 방해 없이 조용히 인풋창 배경에 불빛 피드백 효과 부여
                            inputField.style.backgroundColor = '#f6ffed';
                            setTimeout(() => { inputField.style.backgroundColor = '#fff'; }, 500);
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
