document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".board_write_form");
    const content = document.querySelector("[data-editor-content]");
    const contentInput = document.getElementById("contentInput");

    if (!form || !content || !contentInput) {
        return;
    }

    form.addEventListener("submit", function (event) {
        const plainText = content.innerText.trim();

        if (!plainText) {
            event.preventDefault();
            alert("내용을 입력하세요.");
            content.focus();
            return;
        }

        // contenteditable의 HTML 값을 hidden input에 담아 서버로 전송
        contentInput.value = content.innerHTML;
    });
});