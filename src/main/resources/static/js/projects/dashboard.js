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
        #5f72d9 0% ${todoEnd}%,
        #f59e0b ${todoEnd}% ${progressEnd}%,
        #6d4ce8 ${progressEnd}% ${reviewEnd}%,
        #22c55e ${reviewEnd}% ${doneEnd}%,
        #eef1ff ${doneEnd}% 100%
    )`;

    function getRate(value) {
        const number = Number(value);

        if (Number.isNaN(number)) {
            return 0;
        }

        return Math.min(100, Math.max(0, number));
    }
});