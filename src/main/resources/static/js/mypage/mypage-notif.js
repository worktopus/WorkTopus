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

    // 1. 체크박스 상태 읽기 (HTML ID 기준)
    const isCommentOn = document.getElementById("notif-comment")?.checked ?? true;
    const isNoticeOn = document.getElementById("notif-notice")?.checked ?? true;
    const isFreeOn = document.getElementById("notif-free")?.checked ?? true;
    const isIdeaOn = document.getElementById("notif-idea")?.checked ?? true;
    const isQuestionOn = document.getElementById("notif-question")?.checked ?? true;

    // 2. 체크박스 설정에 맞게 알림 데이터 필터링
    const filteredNotifications = globalNotifications.filter(notif => {
        const type = notif.type;
        const msg = notif.message || "";

        // 댓글 알림
        if (type === 'COMMENT' || msg.includes('댓글')) {
            return isCommentOn;
        }

        // 메시지 문구 내용 기반으로 카테고리 필터링
        if (msg.includes('[공지사항]')) {
            return isNoticeOn;
        }
        if (msg.includes('[자유게시판]')) {
            return isFreeOn;
        }
        if (msg.includes('[아이디어]')) {
            return isIdeaOn;
        }
        if (msg.includes('[질문게시판]')) {
            return isQuestionOn;
        }

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
        const bgStyle = isUnread ? 'background-color: #f0fdf4; border: 1px solid #bbf7d0;' : 'background-color: #ffffff; border: 1px solid #e2e8f0;';

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
    if (event) event.stopPropagation(); // 알림 클릭(페이지 이동) 방지

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

            // 목록 재조회
            loadNotifications();

            // 상단 헤더 알림 팝업/뱃지도 같이 업데이트 (헤더 스크립트 함수 존재 시)
            if (typeof updateUnreadNotifBadge === 'function') {
                updateUnreadNotifBadge();
            }
        })
        .catch(err => alert("알림 삭제 중 오류가 발생했습니다."));
}

// 체크박스를 켜고 끌 때 호출되는 함수
function updateNotifSetting() {
    // 현재 체크 상태를 localStorage에 저장
    const settings = {
        comment: document.getElementById("notif-comment")?.checked,
        notice: document.getElementById("notif-notice")?.checked,
        free: document.getElementById("notif-free")?.checked,
        idea: document.getElementById("notif-idea")?.checked,
        question: document.getElementById("notif-question")?.checked
    };
    localStorage.setItem("notifSettings", JSON.stringify(settings));

    renderNotifications(); // 페이지 재요청 없이 기존 데이터를 필터링해서 다시 그림
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

    if (savedSettings.comment !== undefined) document.getElementById("notif-comment").checked = savedSettings.comment;
    if (savedSettings.notice !== undefined) document.getElementById("notif-notice").checked = savedSettings.notice;
    if (savedSettings.free !== undefined) document.getElementById("notif-free").checked = savedSettings.free;
    if (savedSettings.idea !== undefined) document.getElementById("notif-idea").checked = savedSettings.idea;
    if (savedSettings.question !== undefined) document.getElementById("notif-question").checked = savedSettings.question;
}