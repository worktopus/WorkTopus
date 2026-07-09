document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".board_write_form");
    const content = document.querySelector("[data-editor-content]");
    const contentInput = document.getElementById("contentInput");

    if (!form || !content || !contentInput) {
        return;
    }

    // 수정 화면에서 기존 내용을 hidden input에도 맞춰둠
    contentInput.value = content.innerHTML;

    form.addEventListener("submit", function (event) {
        const plainText = content.innerText.trim();

        if (!plainText) {
            event.preventDefault();
            alert("내용을 입력하세요.");
            content.focus();
            return;
        }

        contentInput.value = content.innerHTML;
    });
});