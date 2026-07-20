
/* ==========================================================
   Worktopus Project Board List
========================================================== */

document.addEventListener("DOMContentLoaded", function () {
    const searchInput = document.querySelector(".board_search-input");
    const searchButton = document.querySelector(".board_search-btn");
    const categorySelect = document.querySelectorAll(".board_select")[0];
    const sortSelect = document.querySelectorAll(".board_select")[1];
    const pageButtons = document.querySelectorAll(".board_page-btn");

    /* ===========================
       검색 실행
    =========================== */
    function submitSearch() {
        const keyword = searchInput ? searchInput.value.trim() : "";
        const category = categorySelect ? categorySelect.value : "전체 카테고리";
        const sort = sortSelect ? sortSelect.value : "최신순";

        console.log("검색어:", keyword);
        console.log("카테고리:", category);
        console.log("정렬:", sort);

        /*
          나중에 실제 DB 연결하면 이런 식으로 변경 가능

          location.href =
              "/project/board?keyword=" + encodeURIComponent(keyword)
              + "&category=" + encodeURIComponent(category)
              + "&sort=" + encodeURIComponent(sort);
        */
    }

    if (searchButton) {
        searchButton.addEventListener("click", submitSearch);
    }

    if (searchInput) {
        searchInput.addEventListener("keydown", function (event) {
            if (event.key === "Enter") {
                submitSearch();
            }
        });
    }

    /* ===========================
       페이지 버튼 활성화
    =========================== */
    pageButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            pageButtons.forEach(function (pageButton) {
                pageButton.classList.remove("is-active");
            });

            button.classList.add("is-active");
        });
    });
});

/* ===========================
   게시글 상세 모달
=========================== */
const boardCards = document.querySelectorAll(".board_card");
const boardModal = document.getElementById("boardDetailModal");
const modalCloseButtons = document.querySelectorAll("[data-modal-close]");

function openBoardModal() {
    if (!boardModal) return;

    boardModal.classList.add("is-open");
    document.body.style.overflow = "hidden";
}

function closeBoardModal() {
    if (!boardModal) return;

    boardModal.classList.remove("is-open");
    document.body.style.overflow = "";
}

boardCards.forEach(function (card) {
    card.addEventListener("click", function (event) {
        event.preventDefault();
        openBoardModal();
    });
});

modalCloseButtons.forEach(function (button) {
    button.addEventListener("click", closeBoardModal);
});

document.addEventListener("keydown", function (event) {
    if (event.key === "Escape") {
        closeBoardModal();
    }
});