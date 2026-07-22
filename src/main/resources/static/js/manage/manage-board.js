// [manage-board.js 상단부 : 아코디언 및 토글 상태 변경 제어]
document.addEventListener('DOMContentLoaded', function () {
    const boardTabContainer = document.getElementById('board-tab');

    // DB 연동 전 화면을 채워줄 모의 게시글 데이터셋
    const mockBoardContents = {
        NOTICE: [
            { title: "🚀 7월 service 정기 패치 완료 안내", writer: "김여진", date: "2026-07-15", views: 42 },
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
        // CSRF 보안 토큰 정보 파싱
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

        const getHeaders = () => {
            const headers = { 'Content-Type': 'application/json' };
            if (csrfToken) headers[csrfHeader] = csrfToken;
            return headers;
        };

        // 하단 모달 주입 함수 호출
        injectManagementModals(csrfToken, csrfHeader, getHeaders);

        // 기능 1 : 토글 스위치 상태 업데이트 API 통신
        boardTabContainer.addEventListener('change', function (e) {
            if (e.target && e.target.classList.contains('board-toggle-switch')) {
                const switchInput = e.target;
                const boardId = switchInput.getAttribute('data-board-id');
                const isActivated = switchInput.checked;

                fetch('/api/manage/board/status-update', {
                    method: 'POST',
                    headers: getHeaders(),
                    body: JSON.stringify({ boardId: parseInt(boardId), activated: isActivated })
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

        // 기능 2 : 글 목록 Row 아코디언 토글 제어
        boardTabContainer.addEventListener('click', function (e) {
            if (e.target && e.target.classList.contains('inspect-board-btn')) {
                const button = e.target;
                const boardType = button.getAttribute('data-board-type');
                const targetDetailRow = document.getElementById(`details-${boardType}`);
                const listWrapper = targetDetailRow.querySelector('.accordion-list-wrapper');

                if (targetDetailRow.style.display === 'table-row') {
                    targetDetailRow.style.display = 'none';
                    button.textContent = '글 목록 열기';
                    button.style.backgroundColor = 'transparent';
                    button.style.color = 'var(--text-sub)';
                    return;
                }

                const allDetailRows = boardTabContainer.querySelectorAll('tr[id^="details-"]');
                allDetailRows.forEach(row => { row.style.display = 'none'; });

                const allInspectButtons = boardTabContainer.querySelectorAll('.inspect-board-btn');
                allInspectButtons.forEach(btn => {
                    btn.textContent = '글 목록 열기';
                    btn.style.backgroundColor = 'transparent';
                    btn.style.color = 'var(--text-sub)';
                });

                listWrapper.innerHTML = '';
                const listData = mockBoardContents[boardType] || [];

                if (listData.length === 0) {
                    listWrapper.innerHTML = '<p style="text-align:left; color:var(--text-sub); font-size:0.85rem; margin:0; padding:8px 0;">작성된 게시글이 존재하지 않습니다.</p>';
                } else {
                    listData.forEach(post => {
                        const postItem = document.createElement('div');
                        postItem.style.cssText = 'display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: #fff; border: 1px solid #eef0f2; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.02);';
                        postItem.innerHTML = `
                            <div style="font-weight: 600; font-size: 0.9rem; color: var(--text-main); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 60%;">${post.title}</div>
                            <div style="display: flex; gap: 16px; font-size: 0.8rem; color: var(--text-sub); white-space: nowrap;">
                                <span>✍️ <span style="font-weight:600; color:#555;">${post.writer}</span></span>
                                <span>📅 ${post.date}</span>
                                <span style="color: var(--primary);">👀 ${post.views}회</span>
                            </div>
                        `;
                        listWrapper.appendChild(postItem);
                    });
                }

                targetDetailRow.style.display = 'table-row';
                button.textContent = '글 목록 닫기';
                button.style.backgroundColor = 'var(--primary)';
                button.style.color = '#fff';
            }
        });

        // 기능 3 : 이름 수정 모달 팝업 연결
        boardTabContainer.addEventListener('click', function (e) {
            if (e.target && e.target.classList.contains('btn-edit')) {
                const row = e.target.closest('tr');
                const boardId = e.target.getAttribute('data-board-id') || '1';
                const currentName = row.querySelector('.board-name-text').textContent.trim();
                const modal = document.getElementById('editBoardModal');
                document.getElementById('editBoardId').value = boardId;
                document.getElementById('editBoardNameInput').value = currentName;
                modal.style.display = 'flex';
            }
        });

        // 기능 4 : 삭제(숨김) 정책 모달 팝업 연결
        boardTabContainer.addEventListener('click', function (e) {
            if (e.target && e.target.classList.contains('btn-delete-board')) {
                const boardId = e.target.getAttribute('data-board-id');
                const row = e.target.closest('tr');
                const boardName = row.querySelector('.board-name-text').textContent.trim();
                const deleteModal = document.getElementById('deleteBoardModal');
                document.getElementById('deleteTargetBoardId').value = boardId;
                document.getElementById('deleteTargetBoardName').textContent = boardName;
                deleteModal.style.display = 'flex';
            }
        });
    }
    // 기능 5 : 화면 하단에 수정/삭제 팝업 모달 HTML 주입 및 전송 엔진
    function injectManagementModals(csrfToken, csrfHeader, getHeaders) {
        if (document.getElementById('editBoardModal')) return;

        const modalContainer = document.createElement('div');
        modalContainer.innerHTML = `
            <div id="editBoardModal" style="display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); z-index:9999; justify-content:center; align-items:center;">
                <div style="background:#fff; padding:24px; border-radius:8px; width:400px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);">
                    <h3 style="margin-top:0; font-size:1.1rem; font-weight:700;">✏️ 게시판 이름 수정</h3>
                    <input type="hidden" id="editBoardId">
                    <div style="margin: 16px 0;">
                        <label style="font-size:0.85rem; color:var(--text-sub); display:block; margin-bottom:6px;">새 게시판 명칭</label>
                        <input type="text" id="editBoardNameInput" style="width:100%; padding:10px; border:1px solid #ddd; border-radius:4px; box-sizing:border-box;">
                    </div>
                    <div style="display:flex; justify-content:flex-end; gap:8px;">
                        <button type="button" id="closeEditModalBtn" style="padding:8px 14px; background:#eef0f2; border:none; border-radius:4px; cursor:pointer;">취소</button>
                        <button type="button" id="submitEditModalBtn" style="padding:8px 14px; background:#007bff; color:#fff; border:none; border-radius:4px; cursor:pointer;">변경 저장</button>
                    </div>
                </div>
            </div>

            <div id="deleteBoardModal" style="display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); z-index:9999; justify-content:center; align-items:center;">
                <div style="background:#fff; padding:24px; border-radius:8px; width:480px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);">
                    <h3 style="margin-top:0; font-size:1.1rem; color:#dc3545; font-weight:700;">⚠️ 게시판 안전 숨김 조치</h3>
                    <input type="hidden" id="deleteTargetBoardId">
                    <p style="font-size:0.9rem; color:#333; margin:12px 0; line-height:1.5;">정말로 [<span id="deleteTargetBoardName" style="font-weight:700; color:#007bff;"></span>] 게시판을 삭제하시겠습니까?</p>
                    <div style="margin: 18px 0; background: #f8f9fa; padding: 12px; border-radius: 6px; border: 1px solid #e9ecef;">
                        <label style="font-size:0.85rem; font-weight:600; color:#444; display:block; margin-bottom:8px;">💡 삭제 후 후속 조치 정책 선택</label>
                        <select id="deletePolicySelect" style="width:100%; padding:8px; border:1px solid #ccc; border-radius:4px; background:#fff; font-size:0.85rem;">
                            <option value="CHAT">게시판 숨김 처리 후 생성자에게 안내 채팅(워크챗) 자동 발송</option>
                            <option value="POPUP">게시판 숨김 처리 후 작성자가 메뉴 진입 시 경고 팝업 문구 노출</option>
                        </select>
                    </div>
                    <div style="display:flex; justify-content:flex-end; gap:8px;">
                        <button type="button" id="closeDeleteModalBtn" style="padding:8px 14px; background:#eef0f2; border:none; border-radius:4px; cursor:pointer;">취소</button>
                        <button type="button" id="submitDeleteModalBtn" style="padding:8px 14px; background:#dc3545; color:#fff; border:none; border-radius:4px; cursor:pointer;">안전 숨김 실행</button>
                    </div>
                </div>
            </div>
        `;
        document.body.appendChild(modalContainer);

        document.getElementById('closeEditModalBtn').addEventListener('click', () => { document.getElementById('editBoardModal').style.display = 'none'; });
        document.getElementById('closeDeleteModalBtn').addEventListener('click', () => { document.getElementById('deleteBoardModal').style.display = 'none'; });

        // 기능 6 : 게시판 명칭 수정 비동기 전송
        document.getElementById('submitEditModalBtn').addEventListener('click', function() {
            const boardId = document.getElementById('editBoardId').value;
            const updatedName = document.getElementById('editBoardNameInput').value.trim();

            if(!updatedName) { alert("게시판 명칭을 입력해주세요."); return; }

            fetch('/api/manage/board/update-name', {
                method: 'POST',
                headers: getHeaders(),
                body: JSON.stringify({ boardId: parseInt(boardId), boardName: updatedName })
            })
                .then(res => {
                    if(res.ok) { alert("게시판 이름 변경이 완료되었습니다."); location.reload(); }
                    else { alert("이름 변경 처리에 실패했습니다."); }
                })
                .catch(err => console.error("Error updating board name:", err));
        });

        // 기능 7 : 게시판 안전 숨김(삭제) 및 정책 연동 비동기 전송
        document.getElementById('submitDeleteModalBtn').addEventListener('click', function() {
            const boardId = document.getElementById('deleteTargetBoardId').value;
            const policy = document.getElementById('deletePolicySelect').value;

            fetch(`/api/manage/board/${boardId}/hide-policy`, {
                method: 'DELETE',
                headers: getHeaders(),
                body: JSON.stringify({ actionPolicy: policy })
            })
                .then(res => {
                    if(res.ok) {
                        const policyMsg = policy === "CHAT"
                            ? "게시판이 숨김 처리되었으며, 작성자에게 안내 채팅이 즉시 발송되었습니다."
                            : "게시판 숨김 처리가 완료되었습니다. 작성자가 해당 게시판 접근 시 경고 팝업이 발생합니다.";
                        alert(`🎉 조치 완료!\n${policyMsg}`);
                        location.reload();
                    } else { alert("게시판 권한 숨김 처리에 실패했습니다."); }
                })
                .catch(err => console.error("Error hiding board with policy:", err));
        });
    }
});
