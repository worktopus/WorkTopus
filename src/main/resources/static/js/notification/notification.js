// 팝업을 사용자가 한 번이라도 열었는지 확인하는 플래그
let isNotificationModalOpened = false;

document.addEventListener("DOMContentLoaded", function () {
    // 페이지 로드 시 안 읽은 알림 뱃지 카운트
    updateUnreadNotifBadge();
});

// 알림 팝업 모달 토글
function toggleNotifModal(event) {
    if (event) event.stopPropagation();

    const notifModal = document.getElementById('notif-popup-modal');
    if (!notifModal) return;

    const isActive = notifModal.classList.contains('active');

    if (!isActive) {
        // 다른 모달(프로필, 메모 등) 닫기 함수가 있다면 호출
        if (typeof closeAllModalsExcept === 'function') {
            closeAllModalsExcept('notif');
        }
        notifModal.classList.add('active');

        // 팝업 열림 상태 기록 및 뱃지 숨기기
        isNotificationModalOpened = true;
        const badge = document.getElementById("header-notif-badge");
        if (badge) badge.style.display = "none";

        loadHeaderNotifications();
    } else {
        notifModal.classList.remove('active');
    }
}

// 헤더 팝업용 알림 목록 조회
function loadHeaderNotifications() {
    fetch('/api/notifications')
        .then(response => {
            if (!response.ok) throw new Error("알림 로드 실패");
            return response.json();
        })
        .then(notifications => {
            renderHeaderNotifications(notifications);
            updateBadgeCount(notifications);
        })
        .catch(error => console.error("알림 조회 에러:", error));
}

// 팝업 내 알림 리스트 HTML 그리기
function renderHeaderNotifications(notifications) {
    const listContainer = document.getElementById("popup-notif-list");
    if (!listContainer) return;

    if (!notifications || notifications.length === 0) {
        listContainer.innerHTML = `
            <li style="padding: 20px; text-align: center; color: #94a3b8; font-size: 13px;">
                새로운 알림이 없습니다.
            </li>
        `;
        return;
    }

    let html = "";
    notifications.forEach(notif => {
        const isUnread = notif.readYn === 'N';
        const bgStyle = isUnread
            ? 'background-color: #f0fdf4; border: 1px solid #bbf7d0;'
            : 'background-color: #f8fafc; border: 1px solid #e2e8f0;';

        html += `
            <li style="padding: 12px 15px; border-radius: 8px; transition: all 0.2s; display: flex; justify-content: space-between; align-items: center; ${bgStyle}">
                <!-- 1. 알림 내용 클릭 구역 -->
                <div style="flex: 1; cursor: pointer;" onclick="onClickHeaderNotification(${notif.id}, '${notif.url}')">
                    <div style="font-size: 13px; font-weight: 600; color: #1e293b; margin-bottom: 3px;">
                        ${notif.message}
                    </div>
                    <div style="font-size: 11px; color: #94a3b8;">
                        ${formatHeaderDate(notif.createdAt)}
                    </div>
                </div>

                <!-- 2. 알림 삭제 버튼 -->
                <button type="button" 
                        onclick="deleteNotification(event, ${notif.id})"
                        style="border: none; background: transparent; color: #94a3b8; font-size: 14px; font-weight: bold; cursor: pointer; padding: 4px 6px; margin-left: 8px; border-radius: 4px;"
                        onmouseover="this.style.color='#ef4444'" 
                        onmouseout="this.style.color='#94a3b8'"
                        title="알림 삭제">
                    ✕
                </button>
            </li>
        `;
    });

    listContainer.innerHTML = html;
}

// 알림 클릭 시 읽음 처리 API 호출
function onClickHeaderNotification(id, targetUrl) {
    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    const headers = { 'Content-Type': 'application/json' };
    if (header && token) {
        headers[header] = token;
    }

    fetch(`/api/notifications/${id}/read`, {
        method: 'PATCH',
        headers: headers
    })
        .then(() => {
            if (targetUrl) {
                location.href = targetUrl;
            } else {
                loadHeaderNotifications();
            }
        })
        .catch(err => console.error(err));
}

// 안 읽은 알림 뱃지 업데이트
function updateBadgeCount(notifications) {
    const badge = document.getElementById("header-notif-badge");
    if (!badge) return;

    if (isNotificationModalOpened) {
        badge.style.display = "none";
        return;
    }

    const unreadCount = notifications.filter(n => n.readYn === 'N').length;
    if (unreadCount > 0) {
        badge.innerText = unreadCount > 99 ? '99+' : unreadCount;
        badge.style.display = "inline-block";
    } else {
        badge.style.display = "none";
    }
}

function updateUnreadNotifBadge() {
    fetch('/api/notifications')
        .then(res => res.json())
        .then(notifications => updateBadgeCount(notifications))
        .catch(() => {});
}

function formatHeaderDate(dateString) {
    if (!dateString) return "";
    const date = new Date(dateString);
    return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
}

// 외부 영역 클릭 시 알림 팝업 닫기
document.addEventListener('click', function (e) {
    const notifModal = document.getElementById('notif-popup-modal');
    const notifBtn = document.getElementById('header-notif-btn');

    if (notifModal && notifModal.classList.contains('active')) {
        if (!notifModal.contains(e.target) && (!notifBtn || !notifBtn.contains(e.target))) {
            notifModal.classList.remove('active');
        }
    }
});

// 알림 삭제 함수
function deleteNotification(event, id) {
    //  알림 클릭 이벤트(페이지 이동 등)가 실행되지 않도록 차단
    if (event) event.stopPropagation();

    const targetLi = event.currentTarget.closest('li');
    if (targetLi) {
        targetLi.remove();
    }

    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    const headers = {};
    if (header && token) {
        headers[header] = token;
    }

    fetch(`/api/notifications/${id}`, {
        method: 'DELETE',
        headers: headers
    })
        .then(response => {
            if (!response.ok) throw new Error("삭제 실패");

        })
        .catch(err => {
            console.error("알림 삭제 중 오류:", err);
            // 에러 발생 시에만 안전하게 재조회
            loadHeaderNotifications();
        });
}