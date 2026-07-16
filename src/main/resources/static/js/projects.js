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