// ==========================================================
// Calendar
// ==========================================================

document.addEventListener("DOMContentLoaded", () => {
    const calendarGrid = document.getElementById("calendarGrid");
    const calendarTitle = document.getElementById("calendarTitle");
    const prevButton = document.getElementById("prevMonth");
    const nextButton = document.getElementById("nextMonth");
    const todayButton = document.getElementById("todayButton");

    const addButton = document.querySelector(".calendar__add-button");
    const modal = document.getElementById("calendarModal");
    const modalClose = document.getElementById("calendarModalClose");
    const modalOverlay = document.querySelector(".calendar-modal__overlay");

    const form = document.getElementById("calendarForm");
    const titleInput = document.getElementById("scheduleTitle");
    const dateInput = document.getElementById("scheduleDate");
    const typeInput = document.getElementById("scheduleType");
    const contentInput = document.getElementById("scheduleContent");
    const modalTitle = document.querySelector(".calendar-modal__header h2");

    const deleteButton = document.getElementById("scheduleDelete");

    const today = new Date();

    let currentYear = today.getFullYear();
    let currentMonth = today.getMonth();
    let editingScheduleId = null;

    const schedules = [
        {
            id: 1,
            title: "팀 회의",
            date: "2026-07-10",
            type: "meeting"
        },
        {
            id: 2,
            title: "칸반보드 마감",
            date: "2026-07-12",
            type: "deadline"
        },
        {
            id: 3,
            title: "1차 화면 리뷰",
            date: "2026-07-15",
            type: "meeting"
        },
        {
            id: 4,
            title: "프론트 배포",
            date: "2026-07-18",
            type: "release"
        }
    ];

    renderCalendar();

    addButton.addEventListener("click", () => {
        openModal();
    });

    modalClose.addEventListener("click", closeModal);
    modalOverlay.addEventListener("click", closeModal);

    form.addEventListener("submit", (event) => {
        event.preventDefault();

        const schedule = {
            id: editingScheduleId || Date.now(),
            title: titleInput.value.trim(),
            date: dateInput.value,
            type: typeInput.value,
            content: contentInput.value.trim()
        };

        if (!schedule.title || !schedule.date) {
            alert("제목과 날짜를 입력해 주세요.");
            return;
        }

        if (editingScheduleId) {
            const index = schedules.findIndex((item) => item.id === editingScheduleId);

            if (index !== -1) {
                schedules[index] = schedule;
            }
        } else {
            schedules.push(schedule);
        }

        renderCalendar();
        form.reset();
        closeModal();
    });

    prevButton.addEventListener("click", () => {
        currentMonth--;

        if (currentMonth < 0) {
            currentMonth = 11;
            currentYear--;
        }

        renderCalendar();
    });

    nextButton.addEventListener("click", () => {
        currentMonth++;

        if (currentMonth > 11) {
            currentMonth = 0;
            currentYear++;
        }

        renderCalendar();
    });

    todayButton.addEventListener("click", () => {
        currentYear = today.getFullYear();
        currentMonth = today.getMonth();

        renderCalendar();
    });

    function renderCalendar() {
        calendarGrid.innerHTML = "";

        calendarTitle.textContent = `${currentYear}년 ${currentMonth + 1}월`;

        const firstDate = new Date(currentYear, currentMonth, 1);
        const firstDay = firstDate.getDay();

        const lastDate = new Date(currentYear, currentMonth + 1, 0);
        const lastDay = lastDate.getDate();

        const prevLastDate = new Date(currentYear, currentMonth, 0).getDate();

        const totalCells = 42;

        for (let i = 0; i < totalCells; i++) {
            const cell = document.createElement("div");
            cell.className = "calendar__cell";

            let dateNumber;
            let cellYear = currentYear;
            let cellMonth = currentMonth;
            let isOtherMonth = false;

            if (i < firstDay) {
                dateNumber = prevLastDate - firstDay + i + 1;
                cellMonth = currentMonth - 1;
                isOtherMonth = true;

                if (cellMonth < 0) {
                    cellMonth = 11;
                    cellYear--;
                }
            } else if (i >= firstDay + lastDay) {
                dateNumber = i - (firstDay + lastDay) + 1;
                cellMonth = currentMonth + 1;
                isOtherMonth = true;

                if (cellMonth > 11) {
                    cellMonth = 0;
                    cellYear++;
                }
            } else {
                dateNumber = i - firstDay + 1;
            }

            const dateString = makeDateString(cellYear, cellMonth, dateNumber);

            if (isOtherMonth) {
                cell.classList.add("is-other-month");
            }

            if (isToday(cellYear, cellMonth, dateNumber)) {
                cell.classList.add("is-today");
            }

            cell.innerHTML = `
                <div class="calendar__date">
                    ${dateNumber}
                </div>

                <div class="calendar__events"></div>
            `;

            renderEvents(cell, dateString);

            calendarGrid.appendChild(cell);
        }
    }

    function renderEvents(cell, dateString) {
        const eventBox = cell.querySelector(".calendar__events");

        const daySchedules = schedules.filter((schedule) => {
            return schedule.date === dateString;
        });

        daySchedules.forEach((schedule) => {
            const event = document.createElement("button");

            event.type = "button";
            event.className = `calendar__event calendar__event--${schedule.type}`;
            event.textContent = schedule.title;

            event.addEventListener("click", (e) => {
                e.stopPropagation();
                openEditModal(schedule.id);
            });

            eventBox.appendChild(event);
        });
    }

    function makeDateString(year, month, date) {
        const monthText = String(month + 1).padStart(2, "0");
        const dateText = String(date).padStart(2, "0");

        return `${year}-${monthText}-${dateText}`;
    }

    function isToday(year, month, date) {
        return (
            year === today.getFullYear() &&
            month === today.getMonth() &&
            date === today.getDate()
        );
    }

    function openModal(date = "") {
        editingScheduleId = null;

        modalTitle.textContent = "일정 추가";
        form.reset();

        if (date) {
            dateInput.value = date;
        }

        modal.classList.add("active");
        titleInput.focus();

        deleteButton.classList.remove("active");
    }

    function openEditModal(scheduleId) {
        const schedule = schedules.find((item) => item.id === scheduleId);

        if (!schedule) {
            return;
        }

        editingScheduleId = schedule.id;

        modalTitle.textContent = "일정 수정";

        titleInput.value = schedule.title;
        dateInput.value = schedule.date;
        typeInput.value = schedule.type;
        contentInput.value = schedule.content || "";

        modal.classList.add("active");
        titleInput.focus();

        deleteButton.classList.add("active");
    }

    function closeModal() {
        modal.classList.remove("active");
        editingScheduleId = null;
    }

    deleteButton.addEventListener("click", () => {
        if (!editingScheduleId) {
            return;
        }

        const result = confirm("이 일정을 삭제하시겠습니까?");

        if (!result) {
            return;
        }

        const index = schedules.findIndex((item) => item.id === editingScheduleId);

        if (index !== -1) {
            schedules.splice(index, 1);
        }

        renderCalendar();
        form.reset();
        closeModal();
    });
});

