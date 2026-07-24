// 전역 변수로 알림 데이터를 보관 (체크박스 클릭할 때 다시 쓰기 위함)
let globalNotifications = [];

document.addEventListener("DOMContentLoaded", function () {
    // 저장된 알림 설정 복원
    restoreNotifSettings();

    // 알림 목록 조회
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
            globalNotifications = notifications; // 가져온 원본 데이터 저장
            renderNotifications(); // 화면 그리기 호출
        })
        .catch(error => {
            console.error("알림을 가져오는 중 에러 발생:", error);
        });
}

// 화면에 알림 목록 그리기 (체크박스 필터링 적용)
function renderNotifications() {
    const notifList = document.getElementById("notif-list");
    if (!notifList) return;

    // 1. 체크박스 상태 읽기
    const isNoticeOn = document.getElementById("notif-notice")?.checked ?? true;
    const isMeetingOn = document.getElementById("notif-meeting")?.checked ?? true;
    const isWorkOn = document.getElementById("notif-work")?.checked ?? true;
    const isResourceOn = document.getElementById("notif-resource")?.checked ?? true;
    const isIdeaOn = document.getElementById("notif-idea")?.checked ?? true;
    const isEtcOn = document.getElementById("notif-etc")?.checked ?? true;
    const isCommentOn = document.getElementById("notif-comment")?.checked ?? true;

    // 2. 체크박스 설정에 맞게 알림 데이터 필터링
    const filteredNotifications = globalNotifications.filter(notif => {
        const type = notif.type;
        const msg = notif.message || "";

        // 댓글 알림 (타입이 COMMENT이거나 댓글 문구 포함 시)
        if (type === 'COMMENT' || msg.includes('댓글')) {
            return isCommentOn;
        }

        // 카테고리 태그 명확하게 매칭 (단순 키워드 제거하고 [태그] 명칭으로만 깔끔하게 비교)
        if (msg.includes('[공지]')) return isNoticeOn;
        if (msg.includes('[회의]')) return isMeetingOn;
        if (msg.includes('[업무]')) return isWorkOn;
        if (msg.includes('[자료]')) return isResourceOn;
        if (msg.includes('[아이디어]')) return isIdeaOn;
        if (msg.includes('[기타]')) return isEtcOn;

        return true;
    });

    // 3. 필터링된 알림이 없을 경우
    if (!filteredNotifications || filteredNotifications.length === 0) {
        notifList.innerHTML = `
            <li style="padding: 20px; border: 1px dashed #cbd5e1; border-radius: 8px; text-align: center; color: #64748b; font-size: 14px; background-color: #f8fafc;">
                새로운 시스템 및 프로젝트 알림이 존재하지 않거나 필터링되었습니다.
            </li>
        `;
        return;
    }

    // 4. 알림 리스트 HTML 생성
    let html = "";
    filteredNotifications.forEach(notif => {
        const isUnread = notif.readYn === 'N';
        const bgStyle = isUnread
            ? 'background-color: #f0fdf4; border: 1px solid #bbf7d0;'
            : 'background-color: #ffffff; border: 1px solid #e2e8f0;';

        html += `
            <li style="padding: 15px 20px; border-radius: 8px; display: flex; justify-content: space-between; align-items: center; ${bgStyle}">
                <!-- 알림 내용 영역 -->
                <div style="cursor: pointer; flex: 1;" onclick="onClickNotification(${notif.id}, '${notif.url}')">
                    <span style="font-weight: bold; color: #1e293b; font-size: 14px;">[${notif.type}]</span>
                    <span style="color: #334155; font-size: 14px; margin-left: 5px;">${notif.message}</span>
                    <div style="font-size: 11px; color: #94a3b8; margin-top: 5px;">${formatDate(notif.createdAt)}</div>
                </div>

                <!-- ✕ 알림 삭제 버튼 -->
                <button type="button" 
                        onclick="deleteNotification(event, ${notif.id})"
                        style="border: none; background: transparent; color: #94a3b8; font-size: 16px; font-weight: bold; cursor: pointer; padding: 4px 8px; margin-left: 12px; border-radius: 4px;"
                        onmouseover="this.style.color='#ef4444'" 
                        onmouseout="this.style.color='#94a3b8'"
                        title="알림 삭제">
                    ✕
                </button>
            </li>
        `;
    });

    notifList.innerHTML = html;
}

// 마이페이지 알림 삭제 기능
function deleteNotification(event, id) {
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
            loadNotifications();
            if (typeof updateUnreadNotifBadge === 'function') {
                updateUnreadNotifBadge();
            }
        })
        .catch(err => alert("알림 삭제 중 오류가 발생했습니다."));
}

// 체크박스를 켜고 끌 때 호출되는 함수
function updateNotifSetting() {
    const settings = {
        notice: document.getElementById("notif-notice")?.checked,
        meeting: document.getElementById("notif-meeting")?.checked,
        work: document.getElementById("notif-work")?.checked,
        resource: document.getElementById("notif-resource")?.checked,
        idea: document.getElementById("notif-idea")?.checked,
        etc: document.getElementById("notif-etc")?.checked,
        comment: document.getElementById("notif-comment")?.checked
    };
    localStorage.setItem("notifSettings", JSON.stringify(settings));

    renderNotifications();
}

// 알림 클릭 시 읽음 처리 API 호출 후 해당 URL로 이동
function onClickNotification(id, targetUrl) {
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
                location.href = targetUrl;
            } else {
                loadNotifications();
            }
        })
        .catch(err => console.error(err));
}

// 날짜 포맷
function formatDate(dateString) {
    if (!dateString) return "";
    const date = new Date(dateString);
    return date.getFullYear() + "-" +
        String(date.getMonth() + 1).padStart(2, '0') + "-" +
        String(date.getDate()).padStart(2, '0') + " " +
        String(date.getHours()).padStart(2, '0') + ":" +
        String(date.getMinutes()).padStart(2, '0');
}

// localStorage에서 설정 가져와 체크박스 상태 복원
function restoreNotifSettings() {
    const savedSettings = JSON.parse(localStorage.getItem("notifSettings") || "{}");

    if (savedSettings.notice !== undefined && document.getElementById("notif-notice")) document.getElementById("notif-notice").checked = savedSettings.notice;
    if (savedSettings.meeting !== undefined && document.getElementById("notif-meeting")) document.getElementById("notif-meeting").checked = savedSettings.meeting;
    if (savedSettings.work !== undefined && document.getElementById("notif-work")) document.getElementById("notif-work").checked = savedSettings.work;
    if (savedSettings.resource !== undefined && document.getElementById("notif-resource")) document.getElementById("notif-resource").checked = savedSettings.resource;
    if (savedSettings.idea !== undefined && document.getElementById("notif-idea")) document.getElementById("notif-idea").checked = savedSettings.idea;
    if (savedSettings.etc !== undefined && document.getElementById("notif-etc")) document.getElementById("notif-etc").checked = savedSettings.etc;
    if (savedSettings.comment !== undefined && document.getElementById("notif-comment")) document.getElementById("notif-comment").checked = savedSettings.comment;
}