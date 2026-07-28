document.addEventListener("DOMContentLoaded", () => {
    const donut = document.querySelector(".donut-chart");

    if (donut) {
        const todoRate = Number(donut.dataset.todoRate) || 0;
        const progressRate = Number(donut.dataset.progressRate) || 0;
        const reviewRate = Number(donut.dataset.reviewRate) || 0;

        const todoEnd = todoRate;
        const progressEnd = todoEnd + progressRate;
        const reviewEnd = progressEnd + reviewRate;

        donut.style.background = `
        conic-gradient(
            #B8C2F7 0% ${todoEnd}%,
            #7287F4 ${todoEnd}% ${progressEnd}%,
            #DFEEF7 ${progressEnd}% ${reviewEnd}%,
            #5D72C9 ${reviewEnd}% 100%
        )
    `;
    }

    function getRate(value) {
        const number = Number(value);

        if (Number.isNaN(number)) {
            return 0;
        }

        return Math.min(100, Math.max(0, number));
    }

    document.querySelectorAll("[data-url]").forEach(card => {
        card.style.cursor = "pointer";

        card.addEventListener("click", () => {
            location.href = card.dataset.url;
        });
    });
});