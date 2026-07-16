/**
 * WorkTopus - 팀원 및 역할 관리 비동기 실시간 렌더링 및 UI 제어 엔진
 */
document.addEventListener('DOMContentLoaded', function () {
    // manage.html 내부에 존재하는 팀원 테이블 tbody 요소를 정확하게 타겟팅합니다.
    const container = document.querySelector('#member-tab table tbody');

    // 스프링 시큐리티 CSRF 보안 토큰 추출
    const csrfTokenInput = document.querySelector('input[name="_csrf"]');
    const csrfToken = csrfTokenInput ? csrfTokenInput.value : '';

    // URL 주소창에서 현재 workspaceId(예: 22)를 안전하게 추출합니다.
    const pathSegments = window.location.pathname.split('/').filter(Boolean);
    const workspaceId = pathSegments[pathSegments.length - 1] || 22;

    /**
     * [핵심 추가] 1. 서버(백엔드)에서 실제 오라클 DB 팀원 데이터를 비동기로 조회해오는 함수
     */
    function loadWorkspaceMembers() {
        // 백엔드 컨트롤러가 모델 데이터를 잘 넘겨주고 있다면, 비동기 호출을 통해 JSON 배열을 가져옵니다.
        fetch(`/api/manage/${workspaceId}/members-data`, {
            method: 'GET'
        })
            .then(response => {
                if (response.ok) return response.json();
                throw new Error('팀원 목록을 불러오는 데 실패했습니다.');
            })
            .then(members => {
                // 가져온 진짜 데이터 배열을 렌더러 함수에 토스합니다.
                renderMemberList(members);
            })
            .catch(error => {
                console.error('Fetch Members Error:', error);
                // 만약 API가 준비 안 되었다면 타임리프 기본 바인딩을 신뢰하므로 경고 로그만 남깁니다.
            });
    }

    /**
     * 2. 오라클 DB에서 넘어온 실제 데이터 구조와 변수명을 1:1로 일치시킨 실시간 HTML 렌더러
     */
    function renderMemberList(members) {
        if (!container) return;
        container.innerHTML = '';

        // 데이터가 없거나 0명일 때 예외 방어 처리
        if (!members || members.length === 0) {
            container.innerHTML = '<tr><td colspan="5" style="padding: 40px; text-align: center; color: var(--text-sub);">현재 참여 중인 팀원이 없습니다.</td></tr>';
            return;
        }

        members.forEach(member => {
            const tr = document.createElement('tr');
            tr.className = 'member-row';
            tr.style.borderBottom = '1px solid var(--border)';

            // 후속 비동기 수정/삭제 통신에서 참조할 수 있도록 데이터 고유 고정 ID(PK)를 명확히 바인딩합니다.
            tr.setAttribute('data-member-id', member.id);

            // DB 직급 데이터(projectRole) 코드를 판별하여 select 박스 초기 활성화 세팅
            const isLeader = member.projectRole === 'LEADER' ? 'selected' : '';
            const isSubLeader = member.projectRole === 'SUB_LEADER' ? 'selected' : '';
            const isMember = member.projectRole === 'MEMBER' ? 'selected' : '';

            // [변수명 완전 교정]: member.name -> member.userName / member.email -> member.userEmail로 변경
            tr.innerHTML = `
                <td style="padding: 16px;">
                    <div style="font-weight: 600;">${member.userName || '이름 없음'}</div>
                    <div style="font-size: 0.8rem; color: var(--text-sub);">${member.userEmail || '이메일 없음'}</div>
                </td>
                <td style="padding: 16px;">
                    <span style="background-color: var(--primary-soft); color: var(--primary); padding: 4px 10px; border-radius: var(--radius-sm); font-size: 0.8rem; font-weight: 700;">
                        ${member.projectRole}
                    </span>
                </td>
                <td style="padding: 16px;">
                    <select class="form-input role-select" data-id="${member.id}" ${member.projectRole === 'LEADER' ? 'disabled' : ''} style="padding: 6px 12px; font-size: 0.85rem;">
                        <option value="LEADER" ${isLeader}>팀장</option>
                        <option value="SUB_LEADER" ${isSubLeader}>부팀장</option>
                        <option value="MEMBER" ${isMember}>팀원</option>
                    </select>
                </td>
                <td style="padding: 16px;">
                    <input type="text" class="form-input task-input" data-id="${member.id}" value="${member.assignedRole || ''}" placeholder="예: UI 디자인, QA" style="padding: 6px 12px; font-size: 0.85rem; width: 80%;">
                </td>
                <td style="padding: 16px; text-align: center;">
                    <button class="btn btn-danger-outline kick-btn" data-id="${member.id}" ${member.projectRole === 'LEADER' ? 'style="display:none;"' : ''} style="padding: 6px 12px; font-size: 0.8rem; cursor: pointer;">제외</button>
                </td>
            `;

            container.appendChild(tr);
        });
    }

    /**
     * 3. 비동기 이벤트 핸들러 바인딩 (직급 변경 및 팀원 추방 기능)
     */
    if (container) {
        // 직급 변경 셀렉트 박스(select) 이벤트 연동
        container.addEventListener('change', function (e) {
            if (e.target && e.target.classList.contains('role-select')) {
                const selectBox = e.target;
                const memberId = selectBox.getAttribute('data-id');
                const selectedRole = selectBox.value;

                fetch('/api/manage/member/role-update', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': csrfToken
                    },
                    body: JSON.stringify({
                        memberId: parseInt(memberId),
                        projectRole: selectedRole
                    })
                })
                    .then(response => {
                        if (response.ok) return response.json();
                        return response.json().then(err => { throw new Error(err.error); });
                    })
                    .then(data => {
                        alert(data.message || '직급이 변경되었습니다.');
                        const roleBadge = selectBox.closest('tr').querySelector('td:nth-child(2) span');
                        if (roleBadge) {
                            roleBadge.textContent = selectedRole;
                        }
                    })
                    .catch(error => {
                        alert('직급 변경 실패: ' + error.message);
                    });
            }
        });

        // 팀원 제외(추방) 버튼 클릭 이벤트 연동
        container.addEventListener('click', function (e) {
            if (e.target && e.target.classList.contains('kick-btn')) {
                const kickBtn = e.target;
                const memberId = kickBtn.getAttribute('data-id');
                const targetRow = kickBtn.closest('tr');

                if (confirm('정말로 이 팀원을 현재 워크스페이스 공간에서 강제 제외하시겠습니까?')) {
                    fetch(`/api/manage/member/${memberId}`, {
                        method: 'DELETE',
                        headers: {
                            'X-CSRF-TOKEN': csrfToken
                        }
                    })
                        .then(response => {
                            if (response.ok) return response.json();
                            return response.json().then(err => { throw new Error(err.error); });
                        })
                        .then(data => {
                            alert(data.message || '팀원이 제외되었습니다.');
                            if (targetRow) targetRow.remove();
                        })
                        .catch(error => {
                            alert('제외 처리 실패: ' + error.message);
                        });
                }
            }
        });
    }

    // [엔진 가동] 페이지가 열릴 때 자동으로 오라클 DB 목록을 한 번 긁어옵니다.
    loadWorkspaceMembers();
});
