// 1. 전역 CSRF 토큰/헤더
const csrfToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

// 2. 프로필 드롭다운 토글
function toggleProfileDropdown(event) {
    if (event) event.stopPropagation();
    const dropdown = document.getElementById('profile-dropdown-menu');
    if (dropdown) dropdown.classList.toggle('active');

    closeAllModalsExcept('profile');
}

// 3. 공통 팝업 닫기 헬퍼
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

// 4. 바깥 영역 클릭 시 드롭다운 및 모달 닫기
document.addEventListener('click', function(e) {
    // 프로필 드롭다운 닫기
    const dropdown = document.getElementById('profile-dropdown-menu');
    if (dropdown) dropdown.classList.remove('active');

    // 메모 모달 닫기
    const todoModal = document.getElementById('todo-popup-modal');
    if (todoModal && todoModal.classList.contains('active')) {
        if (!todoModal.contains(e.target)) {
            if (typeof forceSaveCurrentMemo === 'function') forceSaveCurrentMemo();
            todoModal.classList.remove('active');
        }
    }

    // 프로젝트 스위처 드롭다운 닫기
    const switcherBtn = document.getElementById("projectSwitcherBtn");
    const dropdownList = document.getElementById("projectDropdownList");
    if (switcherBtn && dropdownList) {
        if (!switcherBtn.contains(e.target) && !dropdownList.contains(e.target)) {
            dropdownList.style.display = "none";
        }
    }
});

// 5. DOM 로드 완료 후 실행되는 로직들
document.addEventListener("DOMContentLoaded", function () {

    // 채팅 버튼
    const chatButton = document.getElementById("chatButton");
    if (chatButton) {
        chatButton.addEventListener("click", function () {
            if (typeof openChat === 'function') openChat();
        });
    }

    // 프로젝트 스위처 드롭다운
    const switcherBtn = document.getElementById("projectSwitcherBtn");
    const dropdownList = document.getElementById("projectDropdownList");

    if (switcherBtn && dropdownList) {
        switcherBtn.addEventListener("click", function (e) {
            e.stopPropagation();

            if (dropdownList.style.display === "none" || dropdownList.style.display === "") {
                fetch('/api/projects/my-list')
                    .then(res => {
                        if (!res.ok) throw new Error("헤더 프로젝트 목록 서버 응답 오류");
                        return res.json();
                    })
                    .then(data => {
                        if (!data || data.length === 0) {
                            dropdownList.innerHTML = "<div style='padding:10px 16px; color:#94a3b8; font-size:13px; font-family:sans-serif;'>참여 중인 프로젝트가 없습니다.</div>";
                        } else {
                            let html = "<ul style='list-style:none; padding:0; margin:0; font-family:sans-serif;'>";
                            data.forEach(proj => {
                                html += `<li style='padding:0;'>
                                            <a href="/projects/${proj.id}" 
                                               style='display:block; padding:10px 16px; color:#334155; text-decoration:none; font-size:14px; font-weight:500; transition:background 0.2s;'
                                               onmouseover="this.style.backgroundColor='#f1f5f9'; this.style.color='#4f46e5';" 
                                               onmouseout="this.style.backgroundColor='transparent'; this.style.color='#334155';">
                                                ${proj.name}
                                            </a>
                                         </li>`;
                            });
                            html += "</ul>";
                            dropdownList.innerHTML = html;
                        }
                        dropdownList.style.display = "block";
                    })
                    .catch(err => console.error("❌ 헤더 프로젝트 목록 로드 실패:", err));
            } else {
                dropdownList.style.display = "none";
            }
        });
    }
});