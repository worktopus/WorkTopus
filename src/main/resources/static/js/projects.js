const joinModal = document.getElementById("joinModal");

function openJoinModal() {
    joinModal.classList.add("active");
}

function closeJoinModal() {
    joinModal.classList.remove("active");
}

window.addEventListener("click", function (e) {
    if (e.target.classList.contains("join-modal-bg")) {
        closeJoinModal();
    }
});

window.addEventListener("keydown", function (e) {
    if (e.key === "Escape") {
        closeJoinModal();
    }
});

document.addEventListener("DOMContentLoaded", function () {
    const copyButtons = document.querySelectorAll(".copy-btn");

    copyButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            const inviteCode = button.dataset.code;

            if (!inviteCode) {
                alert("복사할 초대 코드가 없습니다.");
                return;
            }

            navigator.clipboard.writeText(inviteCode)
                .then(function () {
                    button.textContent = "복사됨";

                    setTimeout(function () {
                        button.textContent = "복사";
                    }, 1200);
                })
                .catch(function () {
                    alert("초대 코드 복사에 실패했습니다.");
                });
        });
    });
});