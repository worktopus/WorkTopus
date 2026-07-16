/**
 *  일반 관리 (프로젝트 이름 변경, 로고 이미지 관리, 공개범위 헤더 아이콘 실시간 연동)
 */

document.addEventListener('DOMContentLoaded', function () {
    // HTML 내부의 form.manage-form 요소를 정밀 탐색합니다.
    const generalForm = document.querySelector('form.manage-form') || document.querySelector('#general-tab form');

    if (generalForm) {
        generalForm.addEventListener('submit', function (e) {
            // 브라우저 강제 이동 및 새로고침 차단
            e.preventDefault();

            // 입력창 텍스트 유효성 검사
            const projectNameInput = document.getElementById('projectName');
            const updatedName = projectNameInput ? projectNameInput.value.trim() : '';

            if (!updatedName) {
                alert('프로젝트 이름을 입력해주세요.');
                return;
            }

            // 현재 사용자가 체크한 공개 범위 설정을 감지합니다 (PUBLIC 또는 PRIVATE)
            const selectedVisibilityInput = generalForm.querySelector('input[name="visibility"]:checked');
            const visibilityValue = selectedVisibilityInput ? selectedVisibilityInput.value : 'PUBLIC';

            // 브라우저 현재 주소창에서 workspaceId 추출
            const pathSegments = window.location.pathname.split('/').filter(Boolean);
            const workspaceId = pathSegments[pathSegments.length - 1] || 45;

            // 폼 내부의 모든 바인딩 데이터 통합 수집
            const formData = new FormData(generalForm);

            // 스프링 시큐리티 CSRF 토큰 추출
            const csrfTokenInput = document.querySelector('input[name="_csrf"]');
            const csrfToken = csrfTokenInput ? csrfTokenInput.value : '';

            // 서버 API 호출 가동
            fetch(`/api/manage/${workspaceId}/general-update`, {
                method: 'POST',
                headers: {
                    'X-CSRF-TOKEN': csrfToken
                },
                body: formData
            })
                .then(response => {
                    if (response.ok) {
                        return response.json();
                    }
                    throw new Error('서버 바인딩 요청 실패');
                })
                .then(data => {
                    alert(data.message || '설정이 성공적으로 저장되었습니다.');

                    // [핵심 기능] 상단 헤더 영역 요소를 탐색합니다.
                    const headerProjectNameSpan = document.querySelector('.header__project-name');
                    if (headerProjectNameSpan) {
                        // 선택된 값에 따라 아이콘 이모지를 동적으로 세팅합니다.
                        const icon = (visibilityValue === 'PUBLIC') ? ' 🌐' : ' 🔒';

                        // 헤더 텍스트를 "입력한 이름 + 이모지" 조합으로 즉시 실시간 교체합니다.
                        headerProjectNameSpan.textContent = updatedName + icon;
                    }
                })
                .catch(error => {
                    console.error('Error details:', error);
                    alert('설정 저장 중 오류가 발생했습니다.');
                });
        });
    }

    // 워크스페이스 영구 삭제 버튼 비동기 연동 처리 구역
    const deleteBtn = document.getElementById('deleteWorkspaceBtn');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', function() {
            if (confirm('정말로 이 워크스페이스를 영구적으로 완전 소멸시키겠습니까?')) {
                const pathSegments = window.location.pathname.split('/').filter(Boolean);
                const workspaceId = pathSegments[pathSegments.length - 1] || 45;

                fetch(`/api/manage/${workspaceId}`, {
                    method: 'DELETE'
                })
                    .then(response => {
                        if (response.ok) {
                            alert('워크스페이스가 정상적으로 삭제되었습니다.');
                            window.location.href = '/dashboard';
                        } else {
                            alert('삭제 권한이 없거나 처리에 실패했습니다.');
                        }
                    })
                    .catch(err => console.error('Delete Error:', err));
            }
        });
    }
});
