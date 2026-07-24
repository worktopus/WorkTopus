// [manage-board.js] 1단계 : 오라클 DB 실시간 통계 연동 및 초기화 구역
document.addEventListener('DOMContentLoaded', function () {
    const boardTabContainer = document.getElementById('board-tab');

    if (boardTabContainer) {
        // 📊 현재 브라우저 주소창(URL) 경로에서 현재 워크스페이스 ID(예: 26)를 유연하게 정제해냅니다.
        const uri = window.location.pathname; // /projects/manage/26/boards
        const segments = uri.split('/');
        let workspaceId = null;
        for (let i = 0; i < segments.length; i++) {
            if (segments[i] === 'manage' && i + 1 < segments.length) {
                workspaceId = segments[i + 1];
                break;
            }
        }

        // CSRF 보안 토큰 정보 파싱 (스프링부트 시큐리티 방어막 연동)
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

        const getHeaders = () => {
            const headers = { 'Content-Type': 'application/json' };
            if (csrfToken) headers[csrfHeader] = csrfToken;
            return headers;
        };

        // 하단 이름수정/안전숨김 모달 주입 함수 호출
        injectManagementModals(csrfToken, csrfHeader, getHeaders);

        // 📊 [오라클 연동 실현] 페이지 로드 즉시 대시보드의 '전체 누적 게시글 수'를 DB에서 실시간 카운트해옵니다.
        if (workspaceId) {
            fetch(`/api/manage/${workspaceId}/board-stats`)
                .then(res => res.json())
                .then(data => {
                    if (data && data.totalPosts !== undefined) {
                        document.getElementById('totalPostsCount').textContent = data.totalPosts;
                    }
                })
                .catch(err => console.error("오라클 통계 데이터 수집 실패:", err));
        }

        // 📌 기능 1 : 아래로 위치 조정(▼) 버튼 이벤트 처리
        boardTabContainer.addEventListener('click', function (e) {
            const target = e.target;
            if (target.classList.contains('btn-move-down')) {
                const row = target.closest('tr');
                const tbody = row.parentNode;
                const boardId = row.getAttribute('data-board-id');

                const currentDetail = document.getElementById(`details-${row.querySelector('.inspect-board-btn').getAttribute('data-board-type')}`);
                const nextRow = currentDetail ? currentDetail.nextElementSibling : row.nextElementSibling;

                if (nextRow && !nextRow.id.startsWith('details-')) {
                    const nextNextRow = nextRow.nextElementSibling && nextRow.nextElementSibling.id.startsWith('details-') ? nextRow.nextElementSibling.nextElementSibling : nextRow.nextElementSibling;
                    tbody.insertBefore(row, nextNextRow);
                    if (currentDetail) tbody.insertBefore(currentDetail, row.nextSibling);
                }

                fetch('/api/manage/board/sequence-update', {
                    method: 'POST',
                    headers: getHeaders(),
                    body: JSON.stringify({ boardId: parseInt(boardId), direction: 'DOWN' })
                }).catch(err => console.error('Sequence Save Fail:', err));
            }
        });
        // 📌 [오라클 연동 실현] 글 목록 아코디언 토글 시 진짜 오라클 DB 데이터를 Fetch하여 렌더링
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

                listWrapper.innerHTML = '<p style="text-align:left; color:var(--text-sub); font-size:0.85rem; margin:0; padding:8px 0;">오라클 DB에서 게시글 수집 중...</p>';

                // 💡 [핵심 연동 주소] 백엔드 컨트롤러 단으로 오라클 DB 실제 글 목록 요청 수행
                fetch(`/api/manage/${workspaceId}/board-contents?category=${boardType}`)
                    .then(res => {
                        if (!res.ok) throw new Error("오라클 데이터 조회 실패");
                        return res.json();
                    })
                    .then(listData => {
                        listWrapper.innerHTML = '';

                        if (!listData || listData.length === 0) {
                            listWrapper.innerHTML = '<p style="text-align:left; color:var(--text-sub); font-size:0.85rem; margin:0; padding:8px 0;">작성된 게시글이 존재하지 않습니다.</p>';
                            return;
                        }

                        // SQL Alias 규격 배열(id, title, writer, date, views, isPinned) 수신 루프 실행
                        listData.forEach(post => {
                            const postItem = document.createElement('div');
                            postItem.style.cssText = 'display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: #fff; border: 1px solid #eef0f2; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.02);';

                            // IS_NOTICE 가 'Y' 면 필독 상태로 취급하여 노란색 핀 활성화
                            const isPinned = post.isPinned === 'Y';

                            const pinBtnHtml = isPinned
                                ? `<button type="button" class="btn-toggle-pin pinned" data-post-id="${post.id}" data-board-type="${boardType}" style="padding:4px 8px; background:#ffc107; color:#000; border:none; border-radius:3px; font-size:0.75rem; cursor:pointer; font-weight:600;">📌 고정 해제</button>`
                                : `<button type="button" class="btn-toggle-pin" data-post-id="${post.id}" data-board-type="${boardType}" style="padding:4px 8px; background:#eef0f2; border:none; border-radius:3px; font-size:0.75rem; cursor:pointer; color:#555;">📍 상단 고정</button>`;

                            postItem.innerHTML = `
                                <div style="font-weight: 600; font-size: 0.9rem; color: var(--text-main); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 50%;">
                                    ${isPinned ? '<span style="color:#ffc107; margin-right:4px;">[필독]</span>' : ''}${post.title}
                                </div>
                                <div style="display: flex; gap: 16px; font-size: 0.8rem; color: var(--text-sub); white-space: nowrap; align-items:center;">
                                    <span>✍️ <span style="font-weight:600; color:#555;">${post.writer}</span></span>
                                    <span>📅 ${post.date}</span>
                                    <span style="color: var(--primary); margin-right:8px;">👀 ${post.views}회</span>
                                    ${pinBtnHtml}
                                </div>
                            `;
                            listWrapper.appendChild(postItem);
                        });
                    })
                    .catch(err => {
                        console.error(err);
                        listWrapper.innerHTML = '<p style="text-align:left; color:#dc3545; font-size:0.85rem; margin:0; padding:8px 0;">오라클 실시간 연동 중 에러가 발생했습니다.</p>';
                    });

                targetDetailRow.style.display = 'table-row';
                button.textContent = '글 목록 닫기';
                button.style.backgroundColor = 'var(--primary)';
                button.style.color = '#fff';
            }
        });

        // 이름 수정 모달 팝업 트리거 연결
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

        // 삭제(숨김) 정책 모달 팝업 연결
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

        // 아코디언 내부 [📍 상단 고정 / 📌 고정 해제] 버튼 클릭 처리 핸들러 (오라클 실시간 플래그 스왑 연동)
        boardTabContainer.addEventListener('click', function (e) {
            if (e.target && e.target.classList.contains('btn-toggle-pin')) {
                const button = e.target;
                const postId = button.getAttribute('data-post-id');
                const boardType = button.getAttribute('data-board-type');
                const isCurrentlyPinned = button.classList.contains('pinned');

                fetch('/api/manage/board/post/toggle-pin', {
                    method: 'POST',
                    headers: getHeaders(),
                    body: JSON.stringify({ postId: parseInt(postId), pinned: !isCurrentlyPinned })
                })
                    .then(res => {
                        if (res.ok) {
                            const inspectBtn = boardTabContainer.querySelector(`.inspect-board-btn[data-board-type="${boardType}"]`);
                            if (inspectBtn) {
                                inspectBtn.click(); // 한 번 닫고
                                inspectBtn.click(); // 다시 열어서 오라클 DB에서 정렬 배치를 실시간 새로고침
                            }
                        } else {
                            alert("상단 고정 정책 오라클 업데이트에 실패했습니다.");
                        }
                    })
                    .catch(err => console.error("Error toggling post pin:", err));
            }
        });
    }
    // 화면 하단에 수정/삭제 팝업 모달 HTML 주입 및 전송 엔진 (기존 유지)
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

        // 게시판 명칭 수정 비동기 전송
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

        // 게시판 안전 숨김(삭제) 및 정책 연동 비동기 전송
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
