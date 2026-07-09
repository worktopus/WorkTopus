// ==========================================================
// Kanban Board
// ==========================================================

document.addEventListener("DOMContentLoaded", () => {
    const lists = document.querySelectorAll(".kanban__list");

    const modal = document.getElementById("taskModal");
    const openBtn = document.querySelector(".kanban__add-button");
    const closeBtn = document.getElementById("modalClose");
    const overlay = document.querySelector(".kanban-modal__overlay");

    const form = document.getElementById("taskForm");
    const titleInput = document.getElementById("taskTitle");
    const userInput = document.getElementById("taskUser");
    const priorityInput = document.getElementById("taskPriority");
    const dateInput = document.getElementById("taskDate");
    const descInput = document.getElementById("taskDesc");

    let draggedCard = null;
    let editingCard = null;

    initCards();
    updateColumnCounts();

    openBtn.addEventListener("click", openModal);
    closeBtn.addEventListener("click", closeModal);
    overlay.addEventListener("click", closeModal);

    form.addEventListener("submit", (event) => {
        event.preventDefault();

        const task = {
            title: titleInput.value.trim(),
            user: userInput.value.trim() || "미정",
            priority: priorityInput.value,
            dueDate: dateInput.value || "마감일 없음",
            desc: descInput.value.trim() || "설명이 없습니다.",
            tags: ["Task"]
        };

        if (!task.title) {
            alert("제목을 입력해 주세요.");
            return;
        }

        if (editingCard) {
            updateTaskCard(editingCard, task);
            editingCard = null;
        } else {
            const todoList = document.getElementById("todoList");

            if (!todoList) {
                console.error("todoList를 찾지 못했습니다.");
                return;
            }

            const card = createTaskCard(task);

            todoList.appendChild(card);
            bindDragEvent(card);
            bindCardMenu(card);
        }

        updateColumnCounts();
        form.reset();
        closeModal();
    });

    lists.forEach((list) => {
        list.addEventListener("dragover", (event) => {
            event.preventDefault();

            const afterElement = getDragAfterElement(list, event.clientY);

            if (!draggedCard) {
                return;
            }

            if (afterElement == null) {
                list.appendChild(draggedCard);
            } else {
                list.insertBefore(draggedCard, afterElement);
            }
        });

        list.addEventListener("drop", () => {
            updateColumnCounts();
        });
    });

    function initCards() {
        const cards = document.querySelectorAll(".kanban__card");

        cards.forEach((card) => {
            bindDragEvent(card);
            bindCardMenu(card);
        });
    }

    function bindDragEvent(card) {
        card.setAttribute("draggable", "true");

        card.addEventListener("dragstart", () => {
            draggedCard = card;
            card.classList.add("is-dragging");
        });

        card.addEventListener("dragend", () => {
            card.classList.remove("is-dragging");
            draggedCard = null;

            updateColumnCounts();
        });

    }

    // CARD 삭제
    function bindCardMenu(card) {

        const moreBtn = card.querySelector(".kanban__more");

        if (!moreBtn) {
            return;
        }

        moreBtn.addEventListener("click", () => {

            const result = confirm("이 업무를 삭제하시겠습니까?");

            if (!result) {
                return;
            }

            card.remove();

            updateColumnCounts();

        });

    }
    // CARD 생성
    function createTaskCard(task) {
        const card = document.createElement("article");
        card.className = "kanban__card";

        const priorityLabel = getPriorityLabel(task.priority);

        card.innerHTML = `
            <div class="kanban__card-top">
                <span class="kanban__badge kanban__badge--${task.priority}">
                    ${priorityLabel}
                </span>

                <button type="button" class="kanban__more">
                    ⋯
                </button>
            </div>

            <h3 class="kanban__card-title">
                ${escapeHtml(task.title)}
            </h3>

            <p class="kanban__card-desc">
                ${escapeHtml(task.desc)}
            </p>

            <div class="kanban__tags">
                ${task.tags.map(tag => `<span>${escapeHtml(tag)}</span>`).join("")}
            </div>

            <div class="kanban__card-footer">
                <span class="kanban__avatar">
                    ${escapeHtml(task.user.slice(0, 2))}
                </span>

                <span class="kanban__date">
                    ${formatDate(task.dueDate)}
                </span>
            </div>
        `;

        return card;
    }

    // CARD 수정
    function bindCardMenu(card) {
        const moreBtn = card.querySelector(".kanban__more");
        const menu = card.querySelector(".kanban-menu");
        const editBtn = card.querySelector("[data-action='edit']");
        const deleteBtn = card.querySelector("[data-action='delete']");

        if (!moreBtn || !menu || !editBtn || !deleteBtn) {
            return;
        }

        moreBtn.addEventListener("click", (event) => {
            event.stopPropagation();

            closeAllMenus(menu);
            menu.classList.toggle("active");
        });

        editBtn.addEventListener("click", (event) => {
            event.stopPropagation();

            menu.classList.remove("active");
            openEditModal(card);
        });

        deleteBtn.addEventListener("click", (event) => {
            event.stopPropagation();

            const result = confirm("이 업무를 삭제하시겠습니까?");

            if (!result) {
                return;
            }

            card.remove();
            updateColumnCounts();
        });
    }

    function openEditModal(card) {
        editingCard = card;

        const title = card.querySelector(".kanban__card-title")?.textContent.trim();
        const desc = card.querySelector(".kanban__card-desc")?.textContent.trim();
        const user = card.querySelector(".kanban__avatar")?.textContent.trim();
        const badge = card.querySelector(".kanban__badge");

        titleInput.value = title || "";
        descInput.value = desc || "";
        userInput.value = user || "";

        if (badge.classList.contains("kanban__badge--high")) {
            priorityInput.value = "high";
        } else if (badge.classList.contains("kanban__badge--medium")) {
            priorityInput.value = "medium";
        } else {
            priorityInput.value = "low";
        }

        modal.classList.add("active");
        titleInput.focus();
    }

    function updateTaskCard(card, task) {
        card.querySelector(".kanban__card-title").textContent = task.title;
        card.querySelector(".kanban__card-desc").textContent = task.desc;
        card.querySelector(".kanban__avatar").textContent = task.user.slice(0, 2);
        card.querySelector(".kanban__date").textContent = formatDate(task.dueDate);

        const badge = card.querySelector(".kanban__badge");
        badge.className = `kanban__badge kanban__badge--${task.priority}`;
        badge.textContent = getPriorityLabel(task.priority);
    }


    function getPriorityLabel(priority) {
        if (priority === "high") return "High";
        if (priority === "medium") return "Medium";
        return "Low";
    }

    function formatDate(date) {
        if (date === "마감일 없음") {
            return date;
        }

        const parsedDate = new Date(date);

        if (Number.isNaN(parsedDate.getTime())) {
            return date;
        }

        return `${parsedDate.getMonth() + 1}월 ${parsedDate.getDate()}일`;
    }

    function getDragAfterElement(list, y) {
        const draggableCards = [
            ...list.querySelectorAll(".kanban__card:not(.is-dragging)")
        ];

        return draggableCards.reduce((closest, child) => {
            const box = child.getBoundingClientRect();
            const offset = y - box.top - box.height / 2;

            if (offset < 0 && offset > closest.offset) {
                return {
                    offset,
                    element: child
                };
            }

            return closest;
        }, {
            offset: Number.NEGATIVE_INFINITY
        }).element;
    }

    function updateColumnCounts() {
        const columns = document.querySelectorAll(".kanban__column");

        columns.forEach((column) => {
            const count = column.querySelectorAll(".kanban__card").length;
            const countEl = column.querySelector(".kanban__column-header strong");

            if (countEl) {
                countEl.textContent = count;
            }
        });
    }

    function openModal() {
        modal.classList.add("active");
        titleInput.focus();
    }

    function closeModal() {
        modal.classList.remove("active");
    }

    function escapeHtml(value) {
        return value
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    document.addEventListener("click", () => {
        closeAllMenus();
    });

    function closeAllMenus(exceptMenu = null) {
        const menus = document.querySelectorAll(".kanban-menu");

        menus.forEach((menu) => {
            if (menu !== exceptMenu) {
                menu.classList.remove("active");
            }
        });
    }
});