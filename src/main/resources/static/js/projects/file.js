// ==========================================================
// Project Files
// ==========================================================

document.addEventListener("DOMContentLoaded", () => {
    const searchInput = document.getElementById("fileSearch");
    const typeFilter = document.getElementById("fileTypeFilter");
    const fileRows = document.querySelectorAll(".file__row");
    const searchEmpty = document.getElementById("fileSearchEmpty");

    searchInput.addEventListener("input", filterFiles);
    typeFilter.addEventListener("change", filterFiles);

    function filterFiles() {
        const keyword = searchInput.value.trim().toLowerCase();
        const selectedType = typeFilter.value;
        let visibleCount = 0;

        fileRows.forEach((row) => {
            const fileName = (row.dataset.fileName ?? "").toLowerCase();
            const boardTitle = (row.dataset.boardTitle ?? "").toLowerCase();
            const writerName = (row.dataset.writerName ?? "").toLowerCase();
            const extension = (row.dataset.fileExtension ?? "").toLowerCase();

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
});
