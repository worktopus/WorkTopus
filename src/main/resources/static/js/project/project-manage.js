// 탭 전환 기능
function openTab(evt, tabName) {
    document.querySelectorAll(".tab-content").forEach(el => el.classList.remove("active"));
    document.querySelectorAll(".tab-btn").forEach(el => el.classList.remove("active"));
    document.getElementById(tabName).classList.add("active");
    evt.currentTarget.classList.add("active");
}

// 팀원 초대 팝업 열기/닫기
function toggleInviteModal(show) {
    document.getElementById('invite-modal').style.display = show ? 'flex' : 'none';
}

// 새 게시판 행 추가
function addBoardRow() {
    const tbody = document.querySelector("#board-table tbody");
    const newRow = tbody.insertRow();
    newRow.innerHTML = `
        <td><input type="text" class="form-input" placeholder="새 게시판명"></td>
        <td><input type="text" class="form-input" placeholder="설명 입력"></td>
        <td><input type="number" class="form-input input-short" value="${tbody.rows.length}"></td>
        <td><button class="btn btn--sm-danger" onclick="this.closest('tr').remove()">취소</button></td>
    `;
}
