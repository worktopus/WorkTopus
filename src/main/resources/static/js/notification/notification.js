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
        if (typeof closeAllModalsExcept === 'function') {
            closeAllModalsExcept('notif');
        }
        notifModal.classList.add('active');

        // 🎯 1. 종을 연 '시각'을 브라우저에 저장 (새로고침해도 배지 안 떠오름)
        localStorage.setItem('lastNotifCheckedTime', new Date().toISOString());

        // 🎯 2. 화면의 빨간 배지 즉시 숨김
        const badge = document.getElementById("header-notif-badge");
        if (badge) badge.style.display = "none";

        // 3. 알림 목록 로드 (초록색 배경은 유지됨)
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
        // DB의 readYn 기준 (클릭해서 들어가지 않은 알림은 계속 초록색!)
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
                        onclick="deleteHeaderNotification(event, ${notif.id})"
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

// 알림 클릭 시 읽음 처리 API 호출 (클릭 시 해당 알림 초록불 끄기)
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

// 🎯 안 읽은 알림 뱃지 카운트 단일화 (중복 제거)
function updateBadgeCount(notifications) {
    const badge = document.getElementById("header-notif-badge");
    if (!badge) return;

    const lastChecked = localStorage.getItem('lastNotifCheckedTime');

    // 'N' 상태이면서 + 종을 열었던 시각 이후에 새로 들어온 알림만 배지 숫자로 표시
    const newUnreadCount = notifications.filter(n => {
        if (n.readYn !== 'N') return false;
        if (!lastChecked) return true;

        // 알림 생성 시간이 종을 마지막으로 누른 시간보다 뒤인 경우에만 뱃지 카운트
        return new Date(n.createdAt) > new Date(lastChecked);
    }).length;

    if (newUnreadCount > 0) {
        badge.innerText = newUnreadCount > 99 ? '99+' : newUnreadCount;
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

// 헤더 알림 삭제 함수
function deleteHeaderNotification(event, id) {
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
            loadHeaderNotifications();
            if (typeof loadNotifications === 'function') {
                loadNotifications();
            }
        })
        .catch(err => {
            console.error("알림 삭제 중 오류:", err);
            loadHeaderNotifications();
        });
}