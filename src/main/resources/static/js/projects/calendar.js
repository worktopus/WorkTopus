// ==========================================================
// Calendar
// ==========================================================

document.addEventListener("DOMContentLoaded", () => {
    const calendar = document.querySelector(".calendar");
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
    const endDateInput = document.getElementById("scheduleEndDate");
    const typeInput = document.getElementById("scheduleType");
    const contentInput = document.getElementById("scheduleContent");
    const modalTitle = document.querySelector(".calendar-modal__header h2");

    const deleteButton = document.getElementById("scheduleDelete");

    const today = new Date();
    const apiBase = calendar?.dataset.apiBase;

    let currentYear = today.getFullYear();
    let currentMonth = today.getMonth();
    let editingScheduleId = null;
    let schedules = [];

    loadSchedules();

    addButton.addEventListener("click", () => {
        openModal();
    });

    modalClose.addEventListener("click", closeModal);
    modalOverlay.addEventListener("click", closeModal);

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const schedule = {
            title: titleInput.value.trim(),
            startDate: dateInput.value,
            endDate: endDateInput.value || dateInput.value,
            type: typeInput.value.toUpperCase(),
            description: contentInput.value.trim()
        };

        if (!schedule.title || !schedule.startDate) {
            alert("제목과 시작일을 입력해 주세요.");
            return;
        }

        try {
            if (editingScheduleId) {
                await updateSchedule(editingScheduleId, schedule);
            } else {
                await createSchedule(schedule);
            }

            await loadSchedules();
            form.reset();
            closeModal();
        } catch (error) {
            console.error("캘린더 일정 저장 오류:", error, error?.stack);
            alert("일정을 저장하지 못했습니다.");
        }
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

            cell.addEventListener("click", () => {
                openModal(dateString);
            });

            renderEvents(cell, dateString);

            calendarGrid.appendChild(cell);
        }
    }

    function renderEvents(cell, dateString) {
        const eventBox = cell.querySelector(".calendar__events");

        const daySchedules = schedules.filter((schedule) => {
            return isDateWithinSchedule(dateString, schedule);
        });

        daySchedules.forEach((schedule) => {
            const event = document.createElement("button");

            event.type = "button";
            event.className = `calendar__event calendar__event--${getScheduleTypeValue(schedule)}`;
            event.textContent = schedule.title;

            event.addEventListener("click", (e) => {
                e.stopPropagation();

                if (schedule.readOnly) {
                    return;
                }

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

    async function loadSchedules() {
        if (!apiBase) {
            renderCalendar();
            return;
        }

        try {
            schedules = await requestJson(apiBase, {method: "GET"}) ?? [];
            renderCalendar();
        } catch (error) {
            console.error("캘린더 일정 조회 오류:", error, error?.stack);
            renderCalendar();
        }
    }

    async function createSchedule(schedule) {
        return requestJson(apiBase, {
            method: "POST",
            body: JSON.stringify(schedule)
        });
    }

    async function updateSchedule(scheduleId, schedule) {
        return requestJson(`${apiBase}/${encodeURIComponent(scheduleId)}`, {
            method: "PUT",
            body: JSON.stringify(schedule)
        });
    }

    async function deleteSchedule(scheduleId) {
        const response = await fetch(`${apiBase}/${encodeURIComponent(scheduleId)}`, {
            method: "DELETE"
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
    }

    async function requestJson(url, options) {
        const response = await fetch(url, {
            ...options,
            headers: {
                "Content-Type": "application/json",
                Accept: "application/json",
                ...(options.headers ?? {})
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const contentType = response.headers.get("Content-Type") ?? "";

        if (!contentType.includes("application/json")) {
            return null;
        }

        const text = await response.text();

        if (!text) {
            return null;
        }

        try {
            return JSON.parse(text);
        } catch (error) {
            console.warn("JSON 응답 파싱 실패:", error, error?.stack);
            return null;
        }
    }

    function isDateWithinSchedule(dateString, schedule) {
        const startDate = schedule.startDate || schedule.date;
        const endDate = schedule.endDate || startDate;

        return startDate <= dateString && dateString <= endDate;
    }

    function getScheduleTypeValue(schedule) {
        return schedule.typeValue || String(schedule.type ?? "meeting").toLowerCase();
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
            endDateInput.value = date;
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
        dateInput.value = schedule.startDate || schedule.date;
        endDateInput.value = schedule.endDate || schedule.startDate || schedule.date;
        typeInput.value = getScheduleTypeValue(schedule);
        contentInput.value = schedule.description || schedule.content || "";

        modal.classList.add("active");
        titleInput.focus();

        deleteButton.classList.add("active");
    }

    function closeModal() {
        modal.classList.remove("active");
        editingScheduleId = null;
    }

    deleteButton.addEventListener("click", async () => {
        if (!editingScheduleId) {
            return;
        }

        const result = confirm("이 일정을 삭제하시겠습니까?");

        if (!result) {
            return;
        }

        try {
            await deleteSchedule(editingScheduleId);
            await loadSchedules();
            form.reset();
            closeModal();
        } catch (error) {
            console.error("캘린더 일정 삭제 오류:", error, error?.stack);
            alert("일정을 삭제하지 못했습니다.");
        }
    });
});

