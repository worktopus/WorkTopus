// 페이지 로드 시 탭 우선순위 제어 로직 수정 완료
document.addEventListener("DOMContentLoaded", function() {
    const urlParams = new URLSearchParams(window.location.search);
    let activeTab = urlParams.get('tab'); // 1. URL 파라미터가 있는지 먼저 찾음

    if (!activeTab) {
        activeTab = 'info';
    }

    switchTab(activeTab);
});

// 탭 전환
function switchTab(tabName) {
    // 존재하지 않는 탭 처리 방지 예외 코드
    if (!document.getElementById('section-' + tabName)) {
        tabName = 'info';
    }

    document.querySelectorAll('.content-section').forEach(sec => sec.classList.remove('active'));
    document.querySelectorAll('.menu-item').forEach(item => item.classList.remove('active'));

    document.getElementById('section-' + tabName).classList.add('active');
    document.getElementById('menu-' + tabName).classList.add('active');

    // 현재 선택한 유효 탭 정보를 브라우저에 저장
    localStorage.setItem('mypageActiveTab', tabName);

    if (tabName === 'posts') {
        loadMyPosts();
    }
    else if (tabName === 'comments') {
        loadMyComments();
    }
    else if (tabName === 'notif') {
        if (typeof loadNotifications === 'function') {
            loadNotifications();
        }
    }
}