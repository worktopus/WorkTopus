/* ==========================================================
   WorkTopus Project Board List
========================================================== */

document.addEventListener("DOMContentLoaded", () => {

    /* ==========================================================
       DOM
    ========================================================== */

    const searchInput = document.querySelector(".board_search-input");
    const searchButton = document.querySelector(".board_search-btn");

    const selects = document.querySelectorAll(".board_select");
    const categorySelect = selects[0];
    const sortSelect = selects[1];

    const pageButtons = document.querySelectorAll(".board_page-btn");

    const modal = document.querySelector("#boardDetailModal");
    const modalTitle = document.querySelector("#modalBoardTitle");
    const modalWriter = document.querySelector("#modalBoardWriter");
    const modalDate = document.querySelector("#modalBoardDate");
    const modalViewCount = document.querySelector("#modalBoardViewCount");
    const modalCommentCount = document.querySelector("#modalBoardCommentCount");
    const modalType = document.querySelector("#modalBoardType");
    const modalContent = document.querySelector("#modalBoardContent");
    const modalFileList = document.querySelector("#modalBoardFileList");
    const modalEditLink = document.querySelector("#modalBoardEditLink");
    const modalDeleteForm = document.querySelector("#modalBoardDeleteForm");

    /* ==========================================================
       검색
    ========================================================== */

    function submitSearch() {
        const keyword = searchInput?.value.trim() ?? "";
        const category = categorySelect?.value ?? "";
        const sort = sortSelect?.value ?? "";

        console.log("검색어:", keyword);
        console.log("카테고리:", category);
        console.log("정렬:", sort);

        /*
        실제 검색 URL을 연결할 때 사용

        const params = new URLSearchParams({
            keyword,
            category,
            sort
        });

        location.href = `${location.pathname}?${params.toString()}`;
        */
    }

    searchButton?.addEventListener("click", submitSearch);

    searchInput?.addEventListener("keydown", event => {
        if (event.key === "Enter") {
            submitSearch();
        }
    });

    /* ==========================================================
       페이지 버튼
    ========================================================== */

    pageButtons.forEach(button => {
        button.addEventListener("click", () => {
            pageButtons.forEach(pageButton => {
                pageButton.classList.remove("is-active");
            });

            button.classList.add("is-active");
        });
    });

    /* ==========================================================
       게시글 상세 모달
    ========================================================== */

    async function openBoardModal(card) {
        if (!modal) {
            console.error("#boardDetailModal을 찾을 수 없습니다.");
            return;
        }

        const detailUrl = card.dataset.detailUrl;

        if (!detailUrl) {
            console.error("게시글 상세 URL이 없습니다.");
            return;
        }

        try {
            const response = await fetch(detailUrl, {
                headers: {
                    Accept: "application/json"
                }
            });

            if (!response.ok) {
                throw new Error(`게시글 조회 실패: ${response.status}`);
            }

            const board = await response.json();

            renderBoardModal(board);

            modal.classList.add("is-open");
            modal.setAttribute("aria-hidden", "false");

            document.body.style.overflow = "hidden";

        } catch (error) {
            console.error("게시글 모달 조회 오류:", error);
            alert("게시글 내용을 불러오지 못했습니다.");
        }
    }

    function renderBoardModal(board) {
        if (modalTitle) {
            modalTitle.textContent = board.title ?? "제목 없음";
        }

        if (modalWriter) {
            modalWriter.textContent = `👤 ${board.writerName ?? "-"}`;
        }

        if (modalDate) {
            modalDate.textContent = `📅 ${board.createdAt ?? "-"}`;
        }

        if (modalViewCount) {
            modalViewCount.textContent = `👁 ${board.viewCount ?? 0}`;
        }

        if (modalCommentCount) {
            modalCommentCount.textContent = `💬 ${board.commentCount ?? 0}`;
        }

        if (modalType) {
            modalType.textContent = board.notice ? "공지" : "게시글";
        }

        if (modalContent) {
            modalContent.innerHTML = board.content ?? "내용이 없습니다.";
        }

        renderBoardFiles(board.files);
        renderBoardActions(board.id);
    }

    function renderBoardFiles(files) {
        if (!modalFileList) {
            return;
        }

        modalFileList.replaceChildren();

        if (!Array.isArray(files) || files.length === 0) {
            const empty = document.createElement("div");
            empty.className = "board-modal__file board-modal__empty";
            empty.textContent = "첨부파일이 없습니다.";
            modalFileList.appendChild(empty);
            return;
        }

        const projectId = getProjectIdFromPath();

        files.forEach(file => {
            const fileItem = document.createElement("div");
            fileItem.className = "board-modal__file";

            const fileLink = document.createElement("a");
            fileLink.textContent = file.originalName ?? "첨부파일";

            if (projectId && file.id) {
                fileLink.href = `/projects/${projectId}/files/${encodeURIComponent(file.id)}/download`;
            } else {
                fileLink.href = "#";
            }

            fileItem.appendChild(fileLink);
            modalFileList.appendChild(fileItem);
        });
    }

    function renderBoardActions(boardId) {
        const projectId = getProjectIdFromPath();

        if (!projectId || !boardId) {
            return;
        }

        const encodedBoardId = encodeURIComponent(boardId);

        if (modalEditLink) {
            modalEditLink.href = `/projects/${projectId}/boards/${encodedBoardId}/edit`;
        }

        if (modalDeleteForm) {
            modalDeleteForm.action = `/projects/${projectId}/boards/${encodedBoardId}/delete`;
        }
    }

    function getProjectIdFromPath() {
        const match = window.location.pathname.match(/\/projects\/(\d+)(?:\/|$)/);

        return match ? encodeURIComponent(match[1]) : null;
    }

    function closeBoardModal() {
        if (!modal) {
            return;
        }

        modal.classList.remove("is-open");
        modal.setAttribute("aria-hidden", "true");

        document.body.style.overflow = "";
    }

    /* ==========================================================
       이벤트 위임
    ========================================================== */

    document.addEventListener("click", event => {
        const closeButton = event.target.closest("[data-modal-close]");

        if (closeButton) {
            closeBoardModal();
            return;
        }

        const card = event.target.closest(".js-board-open");

        if (!card) {
            return;
        }

        event.preventDefault();
        openBoardModal(card);
    });

    document.addEventListener("keydown", event => {
        if (event.key === "Escape") {
            closeBoardModal();
            return;
        }

        if (event.key !== "Enter" && event.key !== " ") {
            return;
        }

        const card = event.target.closest(".js-board-open");

        if (!card) {
            return;
        }

        event.preventDefault();
        openBoardModal(card);
    });
});
