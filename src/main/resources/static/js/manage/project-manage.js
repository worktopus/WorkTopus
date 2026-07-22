// [project-manage.js : URL 주소 기반 상단 탭 버튼 active 클래스 자동 활성화 제어 엔진]
document.addEventListener("DOMContentLoaded", function () {
    const currentPath = window.location.pathname;
    const tabs = document.querySelectorAll(".manage-tabs .tab-btn");

    tabs.forEach(tab => tab.classList.remove("active"));

    // 현재 브라우저 주소창의 끝자리를 판별하여 불빛 동기화
    if (currentPath.endsWith("/members")) {
        setTabActive("팀원 관리");
    } else if (currentPath.endsWith("/invite")) {
        setTabActive("팀원 초대");
    } else if (currentPath.endsWith("/boards")) {
        setTabActive("게시판 관리");
    } else {
        setTabActive("일반 관리");
    }

    function setTabActive(tabText) {
        tabs.forEach(tab => {
            if (tab.textContent.trim() === tabText) {
                tab.classList.add("active");
            }
        });
    }
});
