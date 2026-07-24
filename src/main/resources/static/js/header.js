document.addEventListener("DOMContentLoaded", function () {

    const chatButton = document.getElementById("chatButton");

    if(chatButton){
        chatButton.addEventListener("click", function () {
            openChat();
        });
    }
});

// 공통 CSRF 토큰 설정
const csrfToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

// 프로필 드롭다운 제어
function toggleProfileDropdown(event) {
    event.stopPropagation();
    const dropdown = document.getElementById('profile-dropdown-menu');
    dropdown.classList.toggle('active');

    // 프로필 켜면 메모 모달은 닫기 (닫기 전에 강제 저장)
    const todoModal = document.getElementById('todo-popup-modal');
    if (todoModal && todoModal.classList.contains('active')) {
        if (typeof forceSaveCurrentMemo === 'function') forceSaveCurrentMemo();
        todoModal.classList.remove('active');
    }
}

// 바깥 영역 클릭 시 드롭다운과 모달 모두 닫기
document.addEventListener('click', function() {
    const dropdown = document.getElementById('profile-dropdown-menu');
    if (dropdown) dropdown.classList.remove('active');

    const modal = document.getElementById('todo-popup-modal');
    if (modal && modal.classList.contains('active')) {
        if (typeof forceSaveCurrentMemo === 'function') forceSaveCurrentMemo();
        modal.classList.remove('active');
    }
});