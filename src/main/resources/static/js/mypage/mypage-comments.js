document.addEventListener("DOMContentLoaded", function () {
    loadMyComments();
});

function loadMyComments() {
    const tbody = document.getElementById("comments-tbody");

    fetch("/user/mypage/comments")
        .then(response => {
            if (!response.ok) throw new Error("댓글 데이터를 불러오지 못했습니다.");
            return response.json();
        })
        .then(data => {
            tbody.innerHTML = ""; // 기존 '불러오는 중...' 문구 제거

            if (data.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="3" style="text-align: center; color: #94a3b8; padding: 30px 0;">
                            작성한 댓글이 없습니다.
                        </td>
                    </tr>`;
                return;
            }

            data.forEach(comment => {
                const tr = document.createElement("tr");

                // 원문 제목 클릭 시 해당 상세페이지(image 예시 경로: /projects/{pId}/boards/{bId})로 이동
                const detailUrl = `/projects/${comment.projectId}/boards/${comment.boardId}`;

                tr.innerHTML = `
                    <td style="font-weight: 500; color: #334155;">${escapeHtml(comment.content)}</td>
                    <td>
                        <a href="${detailUrl}" style="color: #2563eb; text-decoration: none;">
                            ${escapeHtml(comment.boardTitle)}
                        </a>
                    </td>
                    <td style="color: #64748b; font-size: 13px;">${comment.createdAt}</td>
                `;
                tbody.appendChild(tr);
            });
        })
        .catch(error => {
            console.error("Error:", error);
            tbody.innerHTML = `
                <tr>
                    <td colspan="3" style="text-align: center; color: #ef4444; padding: 30px 0;">
                        데이터를 불러오는 중 오류가 발생했습니다.
                    </td>
                </tr>`;
        });
}

// XSS 방지를 위한 HTML 이스케이프 함수
function escapeHtml(text) {
    if (!text) return "";
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}