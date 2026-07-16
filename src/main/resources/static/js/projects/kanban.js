// ==========================================================
// Kanban Board
// ==========================================================

document.addEventListener("DOMContentLoaded", () => {
    const kanban = document.querySelector(".kanban");
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

    const apiBase = kanban?.dataset.apiBase;
    const listsByStatus = new Map(
        [...lists].map((list) => [list.dataset.status, list]));

    const csrfToken =
        document.querySelector('meta[name="_csrf"]')?.content;

    const csrfHeader =
        document.querySelector('meta[name="_csrf_header"]')?.content;


    let draggedCard = null;
    let dragSourceList = null;
    let editingCard = null;

    initCards();
    updateColumnCounts();
    loadTasks();

    openBtn?.addEventListener("click", openModal);
    closeBtn?.addEventListener("click", closeModal);
    overlay?.addEventListener("click", closeModal);

    form?.addEventListener("submit", async (event) => {
        event.preventDefault();

        const task = {
            title: titleInput.value.trim(),
            assignee: userInput.value.trim(),
            priority: priorityInput.value,
            dueDate: dateInput.value || null,
            description: descInput.value.trim()
        };

        if (!task.title) {
            alert("제목을 입력해 주세요.");
            return;
        }

        try {
            if (editingCard) {
                const updatedCard = await updateTask(editingCard.dataset.cardId, task);
                updateTaskCard(editingCard, updatedCard);
                editingCard = null;
            } else {
                const createdCard = await createTask(task);
                const todoList = document.getElementById("todoList");

                if (!todoList) {
                    console.error("todoList를 찾지 못했습니다.");
                    return;
                }

                const card = createTaskCard(createdCard);
                todoList.appendChild(card);
                bindDragEvent(card);
                bindCardMenu(card);
            }

            updateColumnCounts();
            closeModal();
        } catch (error) {
            console.error("칸반 카드 저장 오류:", error);
            alert("업무를 저장하지 못했습니다.");
        }
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

        list.addEventListener("drop", async () => {
            if (!draggedCard) {
                return;
            }

            const currentCard = draggedCard;
            const sourceList = dragSourceList;
            const nextStatus = list.dataset.status;
            const cardId = currentCard.dataset.cardId;

            if (currentCard.dataset.updating === "true") {
                updateColumnCounts();
                return;
            }

            if (!cardId || !nextStatus || currentCard.dataset.status === nextStatus) {
                updateColumnCounts();
                return;
            }

            let updatedCard = null;
            currentCard.dataset.updating = "true";

            try {
                updatedCard = await updateTaskStatus(cardId, nextStatus);
                currentCard.dataset.status = nextStatus;
            } catch (error) {
                console.error("칸반 카드 상태 변경 오류:", error, error?.stack);

                if (sourceList) {
                    sourceList.appendChild(currentCard);
                }

                console.error("칸반 카드 상태 변경 실패 상세:", {
                    message: error?.message,
                    cardId,
                    nextStatus,
                    currentStatus: currentCard.dataset.status
                });
                alert("업무 상태를 변경하지 못했습니다.");
                updateColumnCounts();
                return;
            } finally {
                delete currentCard.dataset.updating;
            }

            if (updatedCard) {
                try {
                    updateTaskCard(currentCard, updatedCard);
                } catch (error) {
                    console.error("칸반 카드 화면 갱신 오류:", error, error?.stack);
                }
            }

            updateColumnCounts();
        });
    });

    function initCards() {
        const cards = document.querySelectorAll(".kanban__card");

        cards.forEach((card) => {
            const list = card.closest(".kanban__list");

            if (list?.dataset.status) {
                card.dataset.status = list.dataset.status;
            }

            bindDragEvent(card);
            bindCardMenu(card);
        });
    }

    async function loadTasks() {
        if (!apiBase) {
            return;
        }

        try {
            const tasks = await requestJson(apiBase, {method: "GET"});

            lists.forEach((list) => {
                list.replaceChildren();
            });

            tasks.forEach((task) => {
                const list = listsByStatus.get(task.status);

                if (!list) {
                    return;
                }

                const card = createTaskCard(task);
                list.appendChild(card);
                bindDragEvent(card);
                bindCardMenu(card);
            });

            updateColumnCounts();
        } catch (error) {
            console.error("칸반 카드 조회 오류:", error);
        }
    }

    function bindDragEvent(card) {
        card.setAttribute("draggable", "true");

        card.addEventListener("dragstart", () => {
            draggedCard = card;
            dragSourceList = card.closest(".kanban__list");
            card.classList.add("is-dragging");
        });

        card.addEventListener("dragend", () => {
            card.classList.remove("is-dragging");
            draggedCard = null;
            dragSourceList = null;

            updateColumnCounts();
        });
    }

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

        deleteBtn.addEventListener("click", async (event) => {
            event.stopPropagation();

            const result = confirm("이 업무를 삭제하시겠습니까?");

            if (!result) {
                return;
            }

            try {
                await deleteTask(card.dataset.cardId);
                card.remove();
                updateColumnCounts();
            } catch (error) {
                console.error("칸반 카드 삭제 오류:", error);
                alert("업무를 삭제하지 못했습니다.");
            }
        });
    }

    function createTaskCard(task) {
        const card = document.createElement("article");
        card.className = "kanban__card";
        applyCardDataset(card, task);

        card.innerHTML = `
            <div class="kanban__card-top">
                <span class="kanban__badge kanban__badge--${getPriorityValue(task.priority)}">
                    ${getPriorityLabel(task.priority)}
                </span>

                <button type="button" class="kanban__more">
                    ⋯
                </button>
            </div>

            <h3 class="kanban__card-title">
                ${escapeHtml(task.title)}
            </h3>

            <p class="kanban__card-desc">
                ${escapeHtml(getDescriptionLabel(task.description))}
            </p>

            <div class="kanban__tags">
                <span>Task</span>
            </div>

            <div class="kanban__card-footer">
                <span class="kanban__avatar">
                    ${escapeHtml(getAssigneeInitial(task.assignee))}
                </span>

                <span class="kanban__date">
                    ${formatDate(task.dueDate)}
                </span>
            </div>

            <div class="kanban-menu">
                <button type="button" class="kanban-menu__button" data-action="edit">
                    수정
                </button>
                <button type="button" class="kanban-menu__button kanban-menu__button--danger" data-action="delete">
                    삭제
                </button>
            </div>
        `;

        return card;
    }

    function openModal() {
        editingCard = null;
        form.reset();
        priorityInput.value = "medium";
        modal.classList.add("active");
        titleInput.focus();
    }

    function openEditModal(card) {
        editingCard = card;

        titleInput.value = card.dataset.title || "";
        descInput.value = card.dataset.description || "";
        userInput.value = card.dataset.assignee || "";
        dateInput.value = card.dataset.dueDate || "";
        priorityInput.value = getPriorityValue(card.dataset.priority || "medium");

        modal.classList.add("active");
        titleInput.focus();
    }

    function updateTaskCard(card, task) {
        applyCardDataset(card, task);

        const title = card.querySelector(".kanban__card-title");
        const desc = card.querySelector(".kanban__card-desc");
        const avatar = card.querySelector(".kanban__avatar");
        const date = card.querySelector(".kanban__date");
        const badge = card.querySelector(".kanban__badge");

        if (title) title.textContent = task.title;
        if (desc) desc.textContent = getDescriptionLabel(task.description);
        if (avatar) avatar.textContent = getAssigneeInitial(task.assignee);
        if (date) date.textContent = formatDate(task.dueDate);

        if (badge) {
            badge.className = `kanban__badge kanban__badge--${getPriorityValue(task.priority)}`;
            badge.textContent = getPriorityLabel(task.priority);
        }
    }

    function applyCardDataset(card, task) {
        card.dataset.cardId = task.id;
        card.dataset.title = task.title ?? "";
        card.dataset.assignee = task.assignee ?? "";
        card.dataset.dueDate = task.dueDate ?? "";
        card.dataset.priority = getPriorityValue(task.priority);
        card.dataset.status = task.status ?? "TODO";
        card.dataset.description = getDescriptionLabel(task.description);
    }

    async function createTask(task) {
        return requestJson(apiBase, {
            method: "POST",
            body: JSON.stringify(toRequestBody(task))
        });
    }

    async function updateTask(cardId, task) {
        return requestJson(`${apiBase}/${encodeURIComponent(cardId)}`, {
            method: "PUT",
            body: JSON.stringify(toRequestBody(task))
        });
    }

    async function updateTaskStatus(cardId, status) {
        return requestJson(`${apiBase}/${encodeURIComponent(cardId)}/status`, {
            method: "PATCH",
            body: JSON.stringify({status})
        });
    }

    async function deleteTask(cardId) {
        const headers = {};

        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        const response = await fetch(
            `${apiBase}/${encodeURIComponent(cardId)}`,
            {
                method: "DELETE",
                headers
            }
        );

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
    }

    async function requestJson(url, options = {}) {
        const headers = {
            "Content-Type": "application/json",
            Accept: "application/json",
            ...(options.headers ?? {})
        };

        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        const response = await fetch(url, {
            ...options,
            headers
        });

        if (!response.ok) {
            const responseText = await response.text();

            console.error("칸반 API 요청 실패:", {
                url,
                method: options.method,
                status: response.status,
                responseText
            });

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
            console.warn("JSON 응답 파싱 실패:", error);
            return null;
        }
    }

    function toRequestBody(task) {
        return {
            title: task.title,
            assignee: task.assignee,
            dueDate: task.dueDate,
            priority: String(task.priority ?? "medium").toUpperCase(),
            description: task.description
        };
    }

    function getPriorityValue(priority) {
        return String(priority ?? "MEDIUM").toLowerCase();
    }

    function getPriorityLabel(priority) {
        const normalizedPriority = String(priority ?? "MEDIUM").toUpperCase();

        if (normalizedPriority === "HIGH") return "High";
        if (normalizedPriority === "LOW") return "Low";
        return "Medium";
    }

    function getDescriptionLabel(description) {
        if (!description || !description.trim()) {
            return "설명이 없습니다.";
        }

        return description;
    }

    function getAssigneeInitial(assignee) {
        const label = assignee && assignee.trim() ? assignee.trim() : "미정";
        return label.length <= 2 ? label : label.slice(0, 2);
    }

    function formatDate(date) {
        if (!date) {
            return "마감일 없음";
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
        const cards = document.querySelectorAll(".kanban__card");
        const inProgressCards = document.querySelectorAll(".kanban__list[data-status='IN_PROGRESS'] .kanban__card");
        const doneCards = document.querySelectorAll(".kanban__list[data-status='DONE'] .kanban__card");

        columns.forEach((column) => {
            const count = column.querySelectorAll(".kanban__card").length;
            const countEl = column.querySelector(".kanban__column-header strong");

            if (countEl) {
                countEl.textContent = count;
            }
        });

        setText("#totalTaskCount", cards.length);
        setText("#inProgressTaskCount", inProgressCards.length);
        setText("#doneTaskCount", doneCards.length);
    }

    function setText(selector, value) {
        const element = document.querySelector(selector);

        if (element) {
            element.textContent = value;
        }
    }

    function closeModal() {
        modal.classList.remove("active");
        editingCard = null;
        form.reset();
    }

    function escapeHtml(value) {
        return String(value ?? "")
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
