document.addEventListener("DOMContentLoaded", function () {
    initProjectSwitcher();
    initHeaderOutsideClick();
});


/* =========================================================
   프로젝트 스위처
========================================================= */

/**
 * 헤더의 프로젝트 스위처 이벤트를 초기화한다.
 */
function initProjectSwitcher() {
    const container = document.querySelector(".header__left-container");
    const dropdownList = document.getElementById("projectDropdownList");

    if (!container || !dropdownList) {
        return;
    }

    let closeTimer = null;
    let isLoaded = false;

    container.addEventListener("mouseenter", async function () {
        clearTimeout(closeTimer);

        if (!isLoaded) {
            await loadProjectDropdown(dropdownList);
            isLoaded = true;
        }

        openProjectDropdown(container);
    });

    container.addEventListener("mouseleave", function () {
        closeTimer = setTimeout(function () {
            closeProjectDropdown(container);
        }, 120);
    });
}


/**
 * 사용자가 참여 중인 프로젝트 목록을 서버에서 조회한다.
 */
async function loadProjectDropdown(dropdownList) {
    try {
        const response = await fetch("/api/projects/my-list");

        if (!response.ok) {
            throw new Error(
                `헤더 프로젝트 목록 조회 실패: ${response.status}`
            );
        }

        const projects = await response.json();

        renderProjectDropdown(dropdownList, projects);
    } catch (error) {
        console.error("헤더 프로젝트 목록 로드 실패:", error);

        dropdownList.innerHTML = `
            <div class="project-dropdown__empty">
                프로젝트 목록을 불러오지 못했습니다.
            </div>
        `;
    }
}


/**
 * 조회한 프로젝트 목록을 드롭다운에 출력한다.
 */
function renderProjectDropdown(dropdownList, projects) {
    if (!Array.isArray(projects) || projects.length === 0) {
        dropdownList.innerHTML = `
            <div class="project-dropdown__empty">
                참여 중인 프로젝트가 없습니다.
            </div>
        `;
        return;
    }

    const projectItems = projects
        .map(function (project) {
            const projectId = encodeURIComponent(project.id);
            const projectName = escapeHtml(project.name);

            return `
                <li class="project-dropdown__item">
                    <a href="/projects/${projectId}"
                       class="project-dropdown__link">
                        ${projectName}
                    </a>
                </li>
            `;
        })
        .join("");

    dropdownList.innerHTML = `
        <ul class="project-dropdown__list">
            ${projectItems}
        </ul>
    `;
}


/**
 * 프로젝트 드롭다운을 연다.
 */
function openProjectDropdown(container) {
    container.classList.add("active");
}


/**
 * 프로젝트 드롭다운을 닫는다.
 */
function closeProjectDropdown(container) {
    if (container) {
        container.classList.remove("active");
    }
}


/* =========================================================
   프로필 드롭다운
========================================================= */

/**
 * 프로필 드롭다운을 열거나 닫는다.
 * HTML의 onclick 속성에서 호출하므로 전역 함수로 선언한다.
 */
function toggleProfileDropdown(event) {
    event.stopPropagation();

    const dropdown = document.getElementById("profile-dropdown-menu");

    if (!dropdown) {
        return;
    }

    const isOpen = dropdown.classList.contains("active");

    closeAllModalsExcept("profile");

    if (isOpen) {
        dropdown.classList.remove("active");
    } else {
        dropdown.classList.add("active");
    }
}


/* =========================================================
   헤더 외부 클릭 처리
========================================================= */

/**
 * 헤더 팝업 외부를 클릭하면 열린 팝업을 닫는다.
 */
function initHeaderOutsideClick() {
    document.addEventListener("click", function (event) {
        closeProfileDropdownOnOutsideClick(event);
        closeProjectDropdownOnOutsideClick(event);
        closeTodoModalOnOutsideClick(event);
        closeNotificationOnOutsideClick(event);
    });
}


/**
 * 프로필 영역 외부 클릭 시 프로필 드롭다운을 닫는다.
 */
function closeProfileDropdownOnOutsideClick(event) {
    const profileContainer = document.querySelector(
        ".header__profile-container"
    );

    const dropdown = document.getElementById(
        "profile-dropdown-menu"
    );

    if (!profileContainer || !dropdown) {
        return;
    }

    if (!profileContainer.contains(event.target)) {
        dropdown.classList.remove("active");
    }
}


/**
 * 프로젝트 스위처 외부 클릭 시 프로젝트 드롭다운을 닫는다.
 */
function closeProjectDropdownOnOutsideClick(event) {
    const container = document.querySelector(
        ".header__left-container"
    );

    if (!container) {
        return;
    }

    if (!container.contains(event.target)) {
        closeProjectDropdown(container);
    }
}


/**
 * 메모 팝업 외부 클릭 시 메모를 저장하고 닫는다.
 */
function closeTodoModalOnOutsideClick(event) {
    const todoModal = document.getElementById(
        "todo-popup-modal"
    );

    if (!todoModal || !todoModal.classList.contains("active")) {
        return;
    }

    const memoButton = document.getElementById(
        "header-memo-btn"
    );

    const clickedInsideModal = todoModal.contains(event.target);
    const clickedMemoButton =
        memoButton && memoButton.contains(event.target);

    if (clickedInsideModal || clickedMemoButton) {
        return;
    }

    if (typeof forceSaveCurrentMemo === "function") {
        forceSaveCurrentMemo();
    }

    todoModal.classList.remove("active");
}

/**
* 알림 영역 외부 클릭 시 알림 팝업을 닫는다.
*/
function closeNotificationOnOutsideClick(event) {
    const notifContainer = document.querySelector(".header__notification-container"); // 실제 HTML 구조에 맞는 부모 클래스명으로 확인 필요
    const notificationModal = document.getElementById("notif-popup-modal");

    if (!notificationModal || !notificationModal.classList.contains("active")) {
        return;
    }

    // 알림 버튼이나 모달 내부를 클릭한 경우는 닫지 않음
    if (notifContainer && notifContainer.contains(event.target)) {
        return;
    }
    if (notificationModal.contains(event.target)) {
        return;
    }

    notificationModal.classList.remove("active");
}


/* =========================================================
   공통 팝업 제어
========================================================= */

/**
 * 현재 선택한 팝업을 제외한 나머지 헤더 팝업을 닫는다.
 */
function closeAllModalsExcept(current) {
    if (current !== "project") {
        const projectContainer = document.querySelector(
            ".header__left-container"
        );

        closeProjectDropdown(projectContainer);
    }

    if (current !== "profile") {
        const profileDropdown = document.getElementById(
            "profile-dropdown-menu"
        );

        if (profileDropdown) {
            profileDropdown.classList.remove("active");
        }
    }

    if (current !== "memo") {
        const todoModal = document.getElementById(
            "todo-popup-modal"
        );

        if (todoModal?.classList.contains("active")) {
            if (typeof forceSaveCurrentMemo === "function") {
                forceSaveCurrentMemo();
            }

            todoModal.classList.remove("active");
        }
    }

    if (current !== "notif") {
        const notificationModal = document.getElementById(
            "notif-popup-modal"
        );

        if (notificationModal) {
            notificationModal.classList.remove("active");
        }
    }
}


/* =========================================================
   문자열 안전 처리
========================================================= */

/**
 * 프로젝트명이 HTML로 해석되지 않도록 특수문자를 치환한다.
 */
function escapeHtml(value) {
    const element = document.createElement("div");

    element.textContent = value ?? "";

    return element.innerHTML;
}

/* =========================================================
   알림 팝업 제어 [신규 추가]
========================================================= */

/**
 * 알림 드롭다운/모달을 열거나 닫는다.
 * HTML의 onclick 속성에서 호출하므로 전역 함수로 선언한다.
 */
function toggleNotificationDropdown(event) {
    if (event) {
        event.stopPropagation();
    }

    const notificationModal = document.getElementById("notif-popup-modal");

    if (!notificationModal) {
        return;
    }

    const isOpen = notificationModal.classList.contains("active");

    // 다른 모든 팝업(프로필, 프로젝트, 메모 등)은 닫기
    closeAllModalsExcept("notif");

    if (isOpen) {
        notificationModal.classList.remove("active");
    } else {
        notificationModal.classList.add("active");

        // (선택) 알림창을 열 때 안 읽은 알림 목록을 서버에서 불러오는 함수가 있다면 여기서 호출
        // if (typeof loadNotifications === "function") { loadNotifications(); }
    }
}