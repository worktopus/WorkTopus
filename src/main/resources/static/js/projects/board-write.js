document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".board_write_form");
    const content = document.querySelector("[data-editor-content]");
    const contentInput = document.getElementById("contentInput");
    const fileInput = document.getElementById("files");
    const selectedFileList = document.getElementById("selectedFileList");
    const selectedFileItems = document.getElementById("selectedFileItems");

    if (fileInput && selectedFileList && selectedFileItems) {
        fileInput.addEventListener("change", function () {
            const files = Array.from(fileInput.files ?? []);

            selectedFileItems.replaceChildren();

            if (files.length === 0) {
                selectedFileList.hidden = true;
                return;
            }

            files.forEach(function (file) {
                const item = document.createElement("li");
                item.textContent = `${file.name} (${formatFileSize(file.size)})`;
                selectedFileItems.appendChild(item);
            });

            selectedFileList.hidden = false;
        });
    }

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

    function formatFileSize(size) {
        if (size < 1024) {
            return `${size} B`;
        }

        const units = ["KB", "MB", "GB", "TB"];
        let formattedSize = size;
        let unitIndex = -1;

        do {
            formattedSize /= 1024;
            unitIndex++;
        } while (formattedSize >= 1024 && unitIndex < units.length - 1);

        return `${Number(formattedSize.toFixed(1))} ${units[unitIndex]}`;
    }
});
