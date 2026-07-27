// [manage-board.js] 1단계 : 기존 프로젝트 게시판 검색/필터 엔진 결합 및 통합 한 줄 리스트 초기화 구역
document.addEventListener('DOMContentLoaded', function () {
    const postContainer = document.getElementById('integratedPostTbody');

    // 📊 현재 브라우저 주소창(URL) 경로에서 현재 워크스페이스(프로젝트) ID를 안전하게 정제해냅니다.
    const uri = window.location.pathname;
    const segments = uri.split('/');
    let projectId = null;
    for (let i = 0; i < segments.length; i++) {
        if (segments[i] === 'manage' && i + 1 < segments.length) {
            projectId = segments[i + 1];
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

    // 📊 [오라클 연동] 페이지 로드 즉시 대시보드의 '전체 누적 게시글 수'를 DB에서 실시간 카운트해옵니다.
    if (projectId) {
        fetch(`/api/manage/${projectId}/board-stats`)
            .then(res => res.json())
            .then(data => {
                if (data && data.totalPosts !== undefined) {
                    document.getElementById('totalPostsCount').textContent = data.totalPosts;
                }
            })
            .catch(err => console.error("오라클 통계 데이터 수집 실패:", err));
    }

    // 📝 [기존 게시판 피드백 결합] 오라클 DB 순수 데이터 바인딩 API 호출 (백엔드 개편 규격 연동)
    function loadInitialProjectPosts() {
        if (!postContainer) return;
        postContainer.innerHTML = '<tr><td colspan="3">오라클 DB에서 프로젝트 통합 게시글 수집 중...</td></tr>';

        fetch(`/api/manage/${projectId}/board-contents`)
            .then(res => {
                if (!res.ok) throw new Error("통합 게시글 로드 실패");
                return res.json();
            })
            .then(listData => {
                renderIntegratedTable(listData);
            })
            .catch(err => {
                console.error(err);
                postContainer.innerHTML = '<tr><td colspan="3">오라클 실시간 데이터 통합 연동 중 에러가 발생했습니다.</td></tr>';
            });
    }

    // 📊 [핵심 렌더링 엔진] 기존 게시판 데이터 규격(소문자 프로퍼티)을 통제 테이블 로우 구조로 드로잉
    function renderIntegratedTable(boards) {
        if (!postContainer) return;
        postContainer.innerHTML = '';

        if (!Array.isArray(boards) || boards.length === 0) {
            postContainer.innerHTML = '<tr><td colspan="3">현재 이 프로젝트 내에 작성된 게시글이 존재하지 않습니다.</td></tr>';
            return;
        }

        boards.forEach(board => {
            const tr = document.createElement('tr');
            tr.className = 'board-item-row';
            tr.setAttribute('data-post-id', board.id);

            // board-list.js 객체 규격 필드명 100% 매핑 동기화
            const noticeTag = board.notice ? '<span>[공지] </span>' : '';
            const contentExcerpt = board.contentPreview ? board.contentPreview : '본문 내용 없음';

            // 날짜 포맷팅 가공 처리 (yyyy.MM.dd)
            let formattedDate = '-';
            if (board.createdAt) {
                formattedDate = String(board.createdAt).slice(0, 10).replaceAll('-', '.');
            }

            tr.innerHTML = `
                <td>
                    <div class="member-name-text">${noticeTag}${board.title || '제목 없음'}</div>
                    <div class="member-email-text">${contentExcerpt}</div>
                </td>
                <td>
                    <div class="member-name-text">${board.writerName || '익명'}</div>
                    <div class="member-email-text">${formattedDate} (👀 ${board.viewCount ?? 0}회)</div>
                </td>
                <td>
                    <button type="button" class="btn btn-edit-post" data-id="${board.id}">수정</button>
                    <button type="button" class="btn btn-delete-post" data-id="${board.id}">삭제</button>
                </td>
            `;
            postContainer.appendChild(tr);
        });
    }

    // 💡 초기 구동
    if (projectId) {
        loadInitialProjectPosts();
    }
    // 📌 [기능 통합 2] 통합 게시글 목록 내 수정 및 삭제 버튼 이벤트 바인딩 구역
    if (postContainer) {
        // 수정 버튼 클릭 트리거 (작성자 무관 전권 수정 작동)
        postContainer.addEventListener('click', function (e) {
            if (e.target && e.target.classList.contains('btn-edit-post')) {
                const targetBtn = e.target;
                const postId = targetBtn.getAttribute('data-id');
                const row = targetBtn.closest('tr');

                // 해당 행에 실시간 바인딩된 제목 텍스트 정밀 추출
                const currentTitle = row.querySelector('.member-name-text').textContent.replace('[공지]', '').trim();
                const modal = document.getElementById('editBoardModal');

                document.getElementById('editBoardId').value = postId;
                document.getElementById('editBoardNameInput').value = currentTitle;
                modal.style.display = 'flex';
            }
        });

        // 삭제 버튼 클릭 트리거 (작성자 무관 전권 삭제 강제 조치)
        postContainer.addEventListener('click', function (e) {
            if (e.target && e.target.classList.contains('btn-delete-post')) {
                const targetBtn = e.target;
                const postId = targetBtn.getAttribute('data-id');
                const row = targetBtn.closest('tr');

                const currentTitle = row.querySelector('.member-name-text').textContent.replace('[공지]', '').trim();
                const deleteModal = document.getElementById('deleteBoardModal');

                document.getElementById('deleteTargetBoardId').value = postId;
                document.getElementById('deleteTargetBoardName').textContent = currentTitle;
                deleteModal.style.display = 'flex';
            }
        });
    }

    // 화면 하단에 수정/삭제 팝업 모달 HTML 주입 및 전송 엔진 (인라인 스타일 완벽 삭제)
    function injectManagementModals(csrfToken, csrfHeader, getHeaders) {
        if (document.getElementById('editBoardModal')) return;

        const modalContainer = document.createElement('div');
        modalContainer.innerHTML = `
            <div id="editBoardModal">
                <div>
                    <h3>✏️ 프로젝트 게시글 제목 수정</h3>
                    <input type="hidden" id="editBoardId">
                    <div>
                        <label>새 게시글 제목</label>
                        <input type="text" id="editBoardNameInput">
                    </div>
                    <div>
                        <button type="button" id="closeEditModalBtn">취소</button>
                        <button type="button" id="submitEditModalBtn">변경 저장</button>
                    </div>
                </div>
            </div>

            <div id="deleteBoardModal">
                <div>
                    <h3>⚠️ 게시글 강제 소멸 조치</h3>
                    <input type="hidden" id="deleteTargetBoardId">
                    <p>정말로 [<span id="deleteTargetBoardName"></span>] 게시글을 전역 삭제하시겠습니까?</p>
                    <div>
                        <label>💡 삭제 후 후속 조치 정책 선택</label>
                        <select id="deletePolicySelect">
                            <option value="CHAT">게시글 완전 삭제 후 작성자에게 안내 채팅(워크챗) 자동 발송</option>
                            <option value="POPUP">게시글 완전 삭제 후 작성자가 메뉴 진입 시 경고 팝업 문구 노출</option>
                        </select>
                    </div>
                    <div>
                        <button type="button" id="closeDeleteModalBtn">취소</button>
                        <button type="button" id="submitDeleteModalBtn">강제 삭제 실행</button>
                    </div>
                </div>
            </div>
        `;
        document.body.appendChild(modalContainer);

        document.getElementById('closeEditModalBtn').addEventListener('click', () => { document.getElementById('editBoardModal').style.display = 'none'; });
        document.getElementById('closeDeleteModalBtn').addEventListener('click', () => { document.getElementById('deleteBoardModal').style.display = 'none'; });

        // 게시글 제목 수정 비동기 전송
        document.getElementById('submitEditModalBtn').addEventListener('click', function() {
            const postId = document.getElementById('editBoardId').value;
            const updatedTitle = document.getElementById('editBoardNameInput').value.trim();

            if(!updatedTitle) { alert("변경할 게시글 제목을 입력해주세요."); return; }

            fetch('/api/manage/board/update-name', {
                method: 'POST',
                headers: getHeaders(),
                body: JSON.stringify({ boardId: parseInt(postId), boardName: updatedTitle })
            })
                .then(res => {
                    if(res.ok) { alert("게시글 제목 변경이 완료되었습니다."); location.reload(); }
                    else { alert("제목 변경 처리에 실패했습니다."); }
                })
                .catch(err => console.error("Error updating post title:", err));
        });

        // 게시글 강제 소멸(삭제) 비동기 전송
        document.getElementById('submitDeleteModalBtn').addEventListener('click', function() {
            const postId = document.getElementById('deleteTargetBoardId').value;
            const policy = document.getElementById('deletePolicySelect').value;

            fetch(`/api/manage/board/${postId}/hide-policy`, {
                method: 'DELETE',
                headers: getHeaders(),
                body: JSON.stringify({ actionPolicy: policy })
            })
                .then(res => {
                    if(res.ok) {
                        const policyMsg = policy === "CHAT"
                            ? "게시글이 완전히 삭제되었으며, 작성자에게 안내 채팅이 즉시 발송되었습니다."
                            : "게시글 완전 삭제가 완료되었습니다. 작성자가 시스템 접근 시 경고 팝업이 발생합니다.";
                        alert(`🎉 조치 완료!\n${policyMsg}`);
                        location.reload();
                    } else { alert("게시글 삭제 처리에 실패했습니다."); }
                })
                .catch(err => console.error("Error deleting post with policy:", err));
        });
    }
});
