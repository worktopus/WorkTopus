document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".board_write_form");
    const content = document.querySelector("[data-editor-content]");
    const contentInput = document.getElementById("contentInput");
    const fileInput = document.getElementById("files");
    const selectedFileList = document.getElementById("selectedFileList");
    const selectedFileItems = document.getElementById("selectedFileItems");
    const maxFileCount = 10;
    const maxFileSize = 10 * 1024 * 1024;
    let selectedFiles = [];

    if (fileInput && selectedFileList && selectedFileItems) {
        fileInput.addEventListener("change", function () {
            const files = Array.from(fileInput.files ?? []);
            const rejectedMessages = [];

            files.forEach(function (file) {
                if (selectedFiles.length >= maxFileCount) {
                    rejectedMessages.push(`파일은 최대 ${maxFileCount}개까지 첨부할 수 있습니다.`);
                    return;
                }

                if (file.size > maxFileSize) {
                    rejectedMessages.push(`${file.name}은 10MB를 초과하여 추가하지 않았습니다.`);
                    return;
                }

                if (hasSameFile(file)) {
                    rejectedMessages.push(`${file.name}은 이미 선택된 파일입니다.`);
                    return;
                }

                selectedFiles.push(file);
            });

            syncFileInput();
            renderSelectedFiles();

            if (rejectedMessages.length > 0) {
                alert([...new Set(rejectedMessages)].join("\n"));
            }
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

    function renderSelectedFiles() {
        selectedFileItems.replaceChildren();

        if (selectedFiles.length === 0) {
            selectedFileList.hidden = true;
            return;
        }

        selectedFiles.forEach(function (file, index) {
            const item = document.createElement("li");
            item.className = "board_write_selected_file";

            const info = document.createElement("div");
            info.className = "board_write_selected_file_info";

            const icon = document.createElement("span");
            icon.className = "board_write_selected_file_icon";
            icon.textContent = "📎";

            const text = document.createElement("div");
            text.className = "board_write_selected_file_text";

            const name = document.createElement("span");
            name.className = "board_write_selected_file_name";
            name.textContent = file.name;

            const size = document.createElement("span");
            size.className = "board_write_selected_file_size";
            size.textContent = formatFileSize(file.size);

            const removeButton = document.createElement("button");
            removeButton.type = "button";
            removeButton.className = "board_write_selected_file_remove";
            removeButton.textContent = "제거";
            removeButton.addEventListener("click", function () {
                selectedFiles.splice(index, 1);
                syncFileInput();
                renderSelectedFiles();
            });

            text.append(name, size);
            info.append(icon, text);
            item.append(info, removeButton);
            selectedFileItems.appendChild(item);
        });

        selectedFileList.hidden = false;
    }

    function syncFileInput() {
        const dataTransfer = new DataTransfer();

        selectedFiles.forEach(function (file) {
            dataTransfer.items.add(file);
        });

        fileInput.files = dataTransfer.files;
    }

    function hasSameFile(targetFile) {
        return selectedFiles.some(function (file) {
            return file.name === targetFile.name && file.size === targetFile.size;
        });
    }

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
