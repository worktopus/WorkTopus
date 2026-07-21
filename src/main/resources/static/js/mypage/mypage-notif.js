document.addEventListener("DOMContentLoaded", function () {
    // 페이지가 로드되면 알림 목록 조회
    loadNotifications();
});

// 알림 목록 불러오기 (API 호출)
function loadNotifications() {
    fetch('/api/notifications')
        .then(response => {
            if (!response.ok) throw new Error("알림 로드 실패");
            return response.json();
        })
        .then(notifications => {
            renderNotifications(notifications);
        })
        .catch(error => {
            console.error("알림을 가져오는 중 에러 발생:", error);
        });
}

// 화면에 알림 목록 그리기
function renderNotifications(notifications) {
    const notifList = document.getElementById("notif-list");

    // 알림이 없을 경우
    if (!notifications || notifications.length === 0) {
        notifList.innerHTML = `
            <li style="padding: 20px; border: 1px dashed #cbd5e1; border-radius: 8px; text-align: center; color: #64748b; font-size: 14px; background-color: #f8fafc;">
                새로운 시스템 및 프로젝트 알림이 존재하지 않습니다.
            </li>
        `;
        return;
    }

    // 알림 리스트 생성
    let html = "";
    notifications.forEach(notif => {
        // 안 읽은 알림 강조 스타일
        const isUnread = notif.readYn === 'N';
        const bgStyle = isUnread ? 'background-color: #f0fdf4; border: 1px solid #bbf7d0;' : 'background-color: #ffffff; border: 1px solid #e2e8f0;';

        html += `
            <li style="padding: 15px 20px; border-radius: 8px; display: flex; justify-content: space-between; align-items: center; ${bgStyle}">
                <div style="cursor: pointer;" onclick="onClickNotification(${notif.id}, '${notif.url}')">
                    <span style="font-weight: bold; color: #1e293b; font-size: 14px;">[${notif.type}]</span>
                    <span style="color: #334155; font-size: 14px; margin-left: 5px;">${notif.message}</span>
                    <div style="font-size: 11px; color: #94a3b8; margin-top: 5px;">${formatDate(notif.createdAt)}</div>
                </div>
            </li>
        `;
    });

    notifList.innerHTML = html;
}

// 알림 클릭 시 읽음 처리 API 호출 후 해당 페이지(URL)로 이동
function onClickNotification(id, targetUrl) {
    // CSRF 토큰 가져오기 (HTML meta 태그 기반)
    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    fetch(`/api/notifications/${id}/read`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        }
    })
        .then(() => {
            if (targetUrl) {
                location.href = targetUrl; // 알림 대상 게시글로 이동
            } else {
                loadNotifications(); // 페이지 이동 주소가 없으면 목록 새로고침
            }
        })
        .catch(err => console.error(err));
}

// 날짜 포맷 함수 (YYYY-MM-DD HH:mm)
function formatDate(dateString) {
    if (!dateString) return "";
    const date = new Date(dateString);
    return date.getFullYear() + "-" +
        String(date.getMonth() + 1).padStart(2, '0') + "-" +
        String(date.getDate()).padStart(2, '0') + " " +
        String(date.getHours()).padStart(2, '0') + ":" +
        String(date.getMinutes()).padStart(2, '0');
}

// 온오프 버튼 클릭 시 (추후 DB 설정 연동용 임시 함수)
function updateNotifSetting() {
    console.log("알림 설정 변경됨");
}