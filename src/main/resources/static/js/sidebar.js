// 사이드 바 동작
document.addEventListener("DOMContentLoaded", () => {
    const layout = document.querySelector(".layout");
    const sidebar = document.getElementById("sidebar");
    const toggle = document.getElementById("sidebarToggle");

    if (!layout || !sidebar || !toggle) return;

    toggle.addEventListener("click", () => {
        sidebar.classList.toggle("sidebar--collapsed");
        layout.classList.toggle("layout--collapsed");
    });
});