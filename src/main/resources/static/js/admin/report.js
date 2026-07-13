
    const tabButtons = document.querySelectorAll(".report-tabs__button");
    const reportRows = document.querySelectorAll(
    ".report-table tbody tr[data-report-type]"
    );

    tabButtons.forEach(function (button) {

    button.addEventListener("click", function () {

        const selectedType = button.dataset.tab;

        tabButtons.forEach(function (tabButton) {
            tabButton.classList.remove(
                "report-tabs__button--active"
            );
        });

        button.classList.add(
            "report-tabs__button--active"
        );

        reportRows.forEach(function (row) {

            const reportType = row.dataset.reportType;

            if (
                selectedType === "all" ||
                reportType === selectedType
            ) {
                row.hidden = false;
            } else {
                row.hidden = true;
            }

        });

    });

});
