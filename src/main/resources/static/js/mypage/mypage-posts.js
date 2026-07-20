// 내 글 목록 조회 주소 변경
function loadMyPosts() {
    fetch("/user/mypage/post")
        .then(res => res.json())
        .then(data => {
            const tbody = document.getElementById('posts-tbody');
            tbody.innerHTML = '';
            if(data.length === 0) {
                tbody.innerHTML = `<tr><td colspan="3" style="text-align:center; color:#94a3b8; padding:30px 0;">작성한 게시글이 없습니다.</td></tr>`;
                return;
            }
            data.forEach(post => {
                const row = `<tr>
                    <td><span class="status-badge">${post.projectName}</span></td>
                    <td>
                        <a href="/projects/${post.projectId}/boards/${post.id}" >${post.title}</a>
                    </td>
                    <td>${post.writeDate}</td>
                </tr>`;
                tbody.innerHTML += row;
            });
        }).catch(err => console.error(err));
}