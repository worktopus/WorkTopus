// homeHeader.js

const csrfToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

function toggleProfileDropdown(event) {
    event.stopPropagation();
    const dropdown = document.getElementById('profile-dropdown-menu');
    dropdown.classList.toggle('active');

    closeAllModalsExcept('profile');
}

// 바깥 영역 클릭 시 프로필 및 메모 닫기
document.addEventListener('click', function(e) {
    const dropdown = document.getElementById('profile-dropdown-menu');
    if (dropdown) dropdown.classList.remove('active');

    const todoModal = document.getElementById('todo-popup-modal');
    if (todoModal && todoModal.classList.contains('active')) {
        if (!todoModal.contains(e.target)) {
            if (typeof forceSaveCurrentMemo === 'function') forceSaveCurrentMemo();
            todoModal.classList.remove('active');
        }
    }
});

// 공통 팝업 닫기 헬퍼
function closeAllModalsExcept(current) {
    if (current !== 'profile') {
        const dropdown = document.getElementById('profile-dropdown-menu');
        if (dropdown) dropdown.classList.remove('active');
    }
    if (current !== 'memo') {
        const todoModal = document.getElementById('todo-popup-modal');
        if (todoModal && todoModal.classList.contains('active')) {
            if (typeof forceSaveCurrentMemo === 'function') forceSaveCurrentMemo();
            todoModal.classList.remove('active');
        }
    }
    if (current !== 'notif') {
        const notifModal = document.getElementById('notif-popup-modal');
        if (notifModal) notifModal.classList.remove('active');
    }
}