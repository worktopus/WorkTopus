/* ==========================================================
   Worktopus Project Board Search / Filter / Sort
========================================================== */

document.addEventListener("DOMContentLoaded", () => {
    const toolbar = document.querySelector(".board_toolbar");
    const searchInput = document.getElementById("boardSearchInput");
    const categoryFilter = document.getElementById("boardCategoryFilter");
    const sortFilter = document.getElementById("boardSortFilter");
    const boardList = document.getElementById("boardList");
    const pagination = document.getElementById("boardPagination");

    if (
        !toolbar ||
        !searchInput ||
        !categoryFilter ||
        !sortFilter ||
        !boardList
    ) {
        return;
    }

    const projectId = toolbar.dataset.projectId;

    if (!projectId) {
        return;
    }

    const originalBoardList = boardList.innerHTML;

    let debounceTimer = null;
    let requestController = null;

    searchInput.addEventListener("input", () => {
        clearTimeout(debounceTimer);

        debounceTimer = setTimeout(() => {
            applyBoardFilters();
        }, 300);
    });

    categoryFilter.addEventListener("change", () => {
        clearTimeout(debounceTimer);
        applyBoardFilters();
    });

    sortFilter.addEventListener("change", () => {
        clearTimeout(debounceTimer);
        applyBoardFilters();
    });

    function applyBoardFilters() {
        const filters = getCurrentFilters();

        const isDefaultState =
            filters.keyword === "" &&
            filters.category === "" &&
            filters.sort === "latest";

        if (isDefaultState) {
            restoreOriginalList();
            return;
        }

        searchBoards(filters);
    }

    function getCurrentFilters() {
        return {
            keyword: searchInput.value.trim(),
            category: categoryFilter.value,
            sort: sortFilter.value
        };
    }

    async function searchBoards(filters) {
        if (requestController) {
            requestController.abort();
        }

        const controller = new AbortController();
        requestController = controller;

        showLoading();

        try {
            const params = new URLSearchParams();

            if (filters.keyword) {
                params.set("keyword", filters.keyword);
            }

            if (filters.category) {
                params.set("category", filters.category);
            }

            params.set("sort", filters.sort);

            const response = await fetch(
                `/projects/${projectId}/boards/search?${params.toString()}`,
                {
                    method: "GET",
                    headers: {
                        Accept: "application/json"
                    },
                    signal: controller.signal
                }
            );

            if (!response.ok) {
                throw new Error(
                    `게시글 조건 조회 실패: ${response.status}`
                );
            }

            const boards = await response.json();

            if (!isCurrentFilter(filters)) {
                return;
            }

            renderBoards(boards);
        } catch (error) {
            if (error.name === "AbortError") {
                return;
            }

            console.error("게시글 조건 조회 오류:", error);
            showError();
        } finally {
            if (requestController === controller) {
                requestController = null;
            }
        }
    }

    function isCurrentFilter(filters) {
        const currentFilters = getCurrentFilters();

        return (
            currentFilters.keyword === filters.keyword &&
            currentFilters.category === filters.category &&
            currentFilters.sort === filters.sort
        );
    }

    function renderBoards(boards) {
        boardList.replaceChildren();
        hidePagination();

        if (!Array.isArray(boards) || boards.length === 0) {
            boardList.appendChild(
                createStateMessage(
                    "조건에 맞는 게시글이 없습니다.",
                    "검색어나 필터 조건을 변경해 보세요."
                )
            );

            return;
        }

        boards.forEach((board) => {
            boardList.appendChild(createBoardCard(board));
        });
    }

    function createBoardCard(board) {
        const article = document.createElement("article");
        article.className = "board_card";

        const cardTop = document.createElement("div");
        cardTop.className = "board_card-top";

        if (board.notice) {
            const noticeBadge = document.createElement("span");
            noticeBadge.className =
                "board_badge board_badge-notice";
            noticeBadge.textContent = "공지";

            cardTop.appendChild(noticeBadge);
        }

        const priority = document.createElement("span");
        priority.className =
            "board_priority board_priority-normal";
        priority.textContent = "보통";

        cardTop.appendChild(priority);

        const titleLink = document.createElement("a");
        titleLink.className = "board_card-title";
        titleLink.href =
            `/projects/${projectId}/boards/${board.id}`;
        titleLink.textContent = board.title || "제목 없음";

        const description = document.createElement("p");
        description.className = "board_card-desc";
        description.textContent =
            board.contentPreview || "게시글 내용이 없습니다.";

        const meta = document.createElement("div");
        meta.className = "board_card-meta";

        const writer = document.createElement("span");
        writer.textContent =
            `👤 ${board.writerName || "작성자 미상"}`;

        const comments = document.createElement("span");
        comments.textContent =
            `💬 ${board.commentCount ?? 0}`;

        const views = document.createElement("span");
        views.textContent =
            `👁 ${board.viewCount ?? 0}`;

        const createdAt = document.createElement("span");
        createdAt.textContent =
            `📅 ${formatDate(board.createdAt)}`;

        meta.append(
            writer,
            comments,
            views,
            createdAt
        );

        article.append(
            cardTop,
            titleLink,
            description,
            meta
        );

        return article;
    }

    function showLoading() {
        hidePagination();

        boardList.replaceChildren(
            createStateMessage(
                "게시글을 불러오고 있습니다.",
                "잠시만 기다려 주세요."
            )
        );
    }

    function showError() {
        hidePagination();

        boardList.replaceChildren(
            createStateMessage(
                "게시글을 불러오지 못했습니다.",
                "잠시 후 다시 시도해 주세요."
            )
        );
    }

    function createStateMessage(title, description) {
        const state = document.createElement("div");
        state.className = "board_search-state";

        const titleElement = document.createElement("strong");
        titleElement.className = "board_search-state-title";
        titleElement.textContent = title;

        const descriptionElement = document.createElement("p");
        descriptionElement.className =
            "board_search-state-desc";
        descriptionElement.textContent = description;

        state.append(titleElement, descriptionElement);

        return state;
    }

    function restoreOriginalList() {
        clearTimeout(debounceTimer);

        if (requestController) {
            requestController.abort();
            requestController = null;
        }

        boardList.innerHTML = originalBoardList;

        if (pagination) {
            pagination.hidden = false;
        }
    }

    function hidePagination() {
        if (pagination) {
            pagination.hidden = true;
        }
    }

    function formatDate(value) {
        if (!value) {
            return "-";
        }

        return String(value)
            .slice(0, 10)
            .replaceAll("-", ".");
    }
});