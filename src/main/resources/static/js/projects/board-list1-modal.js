document.addEventListener("DOMContentLoaded", () => {
    const modal = document.getElementById("boardDetailModal");
    const openButtons = document.querySelectorAll(".js-board-open");

    if (!modal) return;

    const closeButtons = modal.querySelectorAll("[data-modal-close]");

    const title = document.getElementById("modalBoardTitle");
    const writer = document.getElementById("modalBoardWriter");
    const date = document.getElementById("modalBoardDate");
    const viewCount = document.getElementById("modalBoardViewCount");
    const commentCount = document.getElementById("modalBoardCommentCount");
    const content = document.getElementById("modalBoardContent");

    openButtons.forEach(button => {
        button.addEventListener("click", async (event) => {
            event.preventDefault();

            const detailUrl = button.dataset.detailUrl;

            const response = await fetch(detailUrl);
            const board = await response.json();

            title.textContent = board.title;
            writer.textContent = `👤 ${board.writerName}`;
            date.textContent = `📅 ${board.createdAt}`;
            viewCount.textContent = `👁 ${board.viewCount}`;
            commentCount.textContent = `💬 ${board.commentCount ?? 0}`;
            content.innerHTML = board.content;

            modal.classList.add("is-open");
            modal.setAttribute("aria-hidden", "false");
            document.body.style.overflow = "hidden";
        });
    });

    closeButtons.forEach(button => {
        button.addEventListener("click", () => {
            modal.classList.remove("is-open");
            modal.setAttribute("aria-hidden", "true");
            document.body.style.overflow = "";
        });
    });
});