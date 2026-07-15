// ==========================================================
// Project Files
// ==========================================================

document.addEventListener("DOMContentLoaded", () => {
    const searchInput = document.getElementById("fileSearch");
    const typeFilter = document.getElementById("fileTypeFilter");
    const fileRows = document.querySelectorAll(".file__row");
    const searchEmpty = document.getElementById("fileSearchEmpty");

    if (!searchInput || !typeFilter) {
        return;
    }

    searchInput.addEventListener("input", filterFiles);
    typeFilter.addEventListener("change", filterFiles);
    filterFiles();

    function filterFiles() {
        const keyword = normalize(searchInput.value);
        const selectedType = typeFilter.value;
        let visibleCount = 0;

        fileRows.forEach((row) => {
            const fileName = normalize(
                row.dataset.fileName || row.querySelector(".file__filename")?.textContent
            );
            const boardTitle = normalize(
                row.dataset.boardTitle || row.querySelector(".file__board")?.textContent
            );
            const writerName = normalize(
                row.dataset.writerName || row.children[4]?.textContent
            );
            const extension = normalize(
                row.dataset.fileExtension || row.querySelector(".file__meta")?.textContent
            ).replace(/^\./, "");

            const matchesKeyword =
                fileName.includes(keyword) ||
                boardTitle.includes(keyword) ||
                writerName.includes(keyword);

            const matchesType =
                selectedType === "all" ||
                getTypeGroup(extension) === selectedType;

            const visible = matchesKeyword && matchesType;
            row.hidden = !visible;

            if (visible) {
                visibleCount++;
            }
        });

        if (searchEmpty) {
            searchEmpty.hidden = visibleCount > 0 || fileRows.length === 0;
        }
    }

    function getTypeGroup(extension) {
        const ext = extension.toLowerCase();

        if (ext === "pdf") return "pdf";

        if (["doc", "docx", "hwp", "txt", "md"].includes(ext)) {
            return "doc";
        }

        if (["png", "jpg", "jpeg", "gif", "svg"].includes(ext)) {
            return "image";
        }

        if (["zip", "rar", "7z"].includes(ext)) {
            return "zip";
        }

        return "all";
    }

    function normalize(value) {
        return String(value ?? "").trim().toLowerCase();
    }
});
