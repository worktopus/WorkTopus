document.addEventListener("DOMContentLoaded", () => {
    const donutChart = document.querySelector(".donut-chart");

    if (!donutChart) {
        return;
    }

    const todoRate = getRate(donutChart.dataset.todoRate);
    const progressRate = getRate(donutChart.dataset.progressRate);
    const reviewRate = getRate(donutChart.dataset.reviewRate);
    const doneRate = getRate(donutChart.dataset.doneRate);

    const todoEnd = todoRate;
    const progressEnd = todoEnd + progressRate;
    const reviewEnd = progressEnd + reviewRate;
    const doneEnd = reviewEnd + doneRate;

    donutChart.style.background = `conic-gradient(
    #7287F4 0% ${todoEnd}%,
    #72D2FF ${todoEnd}% ${progressEnd}%,
    #A78BFA ${progressEnd}% ${reviewEnd}%,
    #66D19E ${reviewEnd}% ${doneEnd}%,
    #EEF1FF ${doneEnd}% 100%
    )`;

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