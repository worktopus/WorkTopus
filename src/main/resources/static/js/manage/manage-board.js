/**
 *  게시판 관리 (활성화 토글 제어)
 */
/**
 * 프로젝트 게시판 하단 확장형 아코디언 실시간 비동기 제어 엔진
 */
document.addEventListener('DOMContentLoaded', function () {
    const boardTabContainer = document.getElementById('board-tab');

    // 백엔드 오라클 데이터베이스 연동 전 화면을 채워줄 게시글 데이터셋
    const mockBoardContents = {
        NOTICE: [
            { title: "🚀 7월 서비스 정기 패치 완료 안내", writer: "김여진", date: "2026-07-15", views: 42 },
            { title: "⚠️ 오라클 DB 계정 비밀번호 정기 변경 권고", writer: "김경규", date: "2026-07-14", views: 89 },
            { title: "🎉 협업 공간 오픈!", writer: "김여진", date: "2026-07-01", views: 156 }
        ],
        FREE: [
            { title: "오늘 점심 메뉴 추천받습니다", writer: "신승민", date: "2026-07-15", views: 12 },
            { title: "자바스크립트 fetch 에러 나는데 도와주실 분 ㅠㅠ", writer: "노희진", date: "2026-07-15", views: 24 },
            { title: "이번 주말에 다들 뭐 하시나요?", writer: "석가경", date: "2026-07-13", views: 8 },
            { title: "드디어 아코디언 연동 기능 끝냈네요 깔끔함", writer: "신승민", date: "2026-07-12", views: 55 }
        ]
    };

    if (boardTabContainer) {
        // 스프링 시큐리티 CSRF 보안 토큰 추출
        const csrfTokenInput = document.querySelector('input[name="_csrf"]');
        const csrfToken = csrfTokenInput ? csrfTokenInput.value : '';

        // 1. 토글 스위치 비동기 상태 변경 기능
        boardTabContainer.addEventListener('change', function (e) {
            if (e.target && e.target.classList.contains('board-toggle-switch')) {
                const switchInput = e.target;
                const boardId = switchInput.getAttribute('data-board-id');
                const isActivated = switchInput.checked;

                fetch('/api/manage/board/status-update', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': csrfToken
                    },
                    body: JSON.stringify({
                        boardId: parseInt(boardId),
                        activated: isActivated
                    })
                })
                    .then(response => {
                        if (response.ok) return response.json();
                        throw new Error('게시판 상태 변경에 실패했습니다.');
                    })
                    .catch(error => {
                        console.error('Board Update Error:', error);
                        switchInput.checked = !isActivated;
                    });
            }
        });

        // 2. 글 목록 버튼 클릭 시 바로 밑에 Row 아코디언 오픈/클로즈 트리거
        boardTabContainer.addEventListener('click', function (e) {
            if (e.target && e.target.classList.contains('inspect-board-btn')) {
                const button = e.target;
                const boardType = button.getAttribute('data-board-type'); // NOTICE 또는 FREE

                // 버튼에 대응하는 하단 아코디언 행(tr) 요소를 탐색합니다.
                const targetDetailRow = document.getElementById(`details-${boardType}`);
                const listWrapper = targetDetailRow.querySelector('.accordion-list-wrapper');

                // 2-A. 이미 열려 있는 상태라면 부드럽게 닫아주고 버튼 텍스트를 복구합니다.
                if (targetDetailRow.style.display === 'table-row') {
                    targetDetailRow.style.display = 'none';
                    button.textContent = '글 목록 열기';
                    button.style.backgroundColor = 'transparent';
                    button.style.color = 'var(--text-sub)';
                    return;
                }

                // 2-B. 다른 열려 있는 아코디언들을 일괄적으로 닫아줍니다. (오타 유발 약어 교정)
                const allDetailRows = boardTabContainer.querySelectorAll('tr[id^="details-"]');
                allDetailRows.forEach(row => {
                    row.style.display = 'none';
                });

                const allInspectButtons = boardTabContainer.querySelectorAll('.inspect-board-btn');
                allInspectButtons.forEach(btn => {
                    btn.textContent = '글 목록 열기';
                    btn.style.backgroundColor = 'transparent';
                    btn.style.color = 'var(--text-sub)';
                });

                // 2-C. 데이터를 꽂아줄 타겟 리스트 영역 초기화
                listWrapper.innerHTML = '';
                const listData = mockBoardContents[boardType] || [];

                if (listData.length === 0) {
                    listWrapper.innerHTML = '<p style="text-align:left; color:var(--text-sub); font-size:0.85rem; margin:0; padding:8px 0;">작성된 게시글이 존재하지 않습니다.</p>';
                } else {
                    // 데이터 피드 리스트 생성 및 정밀 정돈 바인딩
                    listData.forEach(post => {
                        const postItem = document.createElement('div');
                        postItem.style.cssText = 'display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: #fff; border: 1px solid #eef0f2; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.02);';

                        // --primary-soft-text 경고 구역을 안전한 var(--primary)로 전면 마감 조치
                        postItem.innerHTML = `
                            <div style="font-weight: 600; font-size: 0.9rem; color: var(--text-main); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 60%;">
                                ${post.title}
                            </div>
                            <div style="display: flex; gap: 16px; font-size: 0.8rem; color: var(--text-sub); white-space: nowrap;">
                                <span>✍️ <span style="font-weight:600; color:#555;">${post.writer}</span></span>
                                <span>📅 ${post.date}</span>
                                <span style="color: var(--primary);">👀 ${post.views}회</span>
                            </div>
                        `;
                        listWrapper.appendChild(postItem);
                    });
                }

                // 2-D. 세팅 완료 후 숨겨져 있던 아코디언 행을 오픈합니다.
                targetDetailRow.style.display = 'table-row';

                // 버튼 스타일도 활성화 상태(닫기)로 피드백 처리합니다.
                button.textContent = '글 목록 닫기';
                button.style.backgroundColor = 'var(--primary)';
                button.style.color = '#fff';
            }
        });
    }
});
