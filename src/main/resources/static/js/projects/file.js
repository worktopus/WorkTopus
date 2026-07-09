// ==========================================================
// Project Files
// ==========================================================

document.addEventListener("DOMContentLoaded", () => {
    const fileList = document.getElementById("fileList");
    const searchInput = document.getElementById("fileSearch");
    const typeFilter = document.getElementById("fileTypeFilter");

    const files = [
        {
            fileId: 1,
            boardId: 101,
            originalName: "7월_1주차_회의록.pdf",
            boardTitle: "프로젝트 착수 회의록",
            size: "245KB",
            extension: "pdf",
            uploader: "린",
            uploadedAt: "2026-07-09"
        },
        {
            fileId: 2,
            boardId: 102,
            originalName: "WorkTopus_ERD.drawio",
            boardTitle: "DB 설계 초안 공유",
            size: "120KB",
            extension: "drawio",
            uploader: "석가",
            uploadedAt: "2026-07-08"
        },
        {
            fileId: 3,
            boardId: 103,
            originalName: "dashboard_ui.png",
            boardTitle: "대시보드 UI 시안",
            size: "1.8MB",
            extension: "png",
            uploader: "규",
            uploadedAt: "2026-07-08"
        },
        {
            fileId: 4,
            boardId: 104,
            originalName: "요구사항정의서.docx",
            boardTitle: "요구사항 정의서 정리",
            size: "312KB",
            extension: "docx",
            uploader: "린",
            uploadedAt: "2026-07-07"
        },
        {
            fileId: 5,
            boardId: 105,
            originalName: "frontend_backup.zip",
            boardTitle: "프론트 백업 파일",
            size: "6.4MB",
            extension: "zip",
            uploader: "노희",
            uploadedAt: "2026-07-06"
        }
    ];

    renderFiles(files);

    searchInput.addEventListener("input", filterFiles);
    typeFilter.addEventListener("change", filterFiles);

    function renderFiles(fileData) {
        fileList.innerHTML = "";

        if (fileData.length === 0) {
            fileList.innerHTML = `
                <div class="file__empty">
                    검색 결과가 없습니다.
                </div>
            `;
            return;
        }

        fileData.forEach((file) => {
            const row = document.createElement("article");
            row.className = "file__row";

            row.innerHTML = `
                <div class="file__name">
                    <span class="file__icon">
                        ${getFileIcon(file.extension)}
                    </span>

                    <div>
                        <div class="file__filename">
                            ${escapeHtml(file.originalName)}
                        </div>
                        <div class="file__meta">
                            .${escapeHtml(file.extension)}
                        </div>
                    </div>
                </div>

                <div class="file__board">
                    ${escapeHtml(file.boardTitle)}
                </div>

                <div class="file__text">
                    ${escapeHtml(file.size)}
                </div>

                <div class="file__text">
                    ${escapeHtml(file.uploadedAt)}
                </div>

                <div class="file__text">
                    ${escapeHtml(file.uploader)}
                </div>

                <div class="file__actions">
                    <button type="button" class="file__button">
                        다운로드
                    </button>

                    <button type="button"
                            class="file__button file__button--sub"
                            data-board-id="${file.boardId}">
                        게시글
                    </button>
                </div>
            `;

            fileList.appendChild(row);
        });
    }

    function filterFiles() {
        const keyword = searchInput.value.trim().toLowerCase();
        const selectedType = typeFilter.value;

        const filteredFiles = files.filter((file) => {
            const matchesKeyword =
                file.originalName.toLowerCase().includes(keyword) ||
                file.boardTitle.toLowerCase().includes(keyword) ||
                file.uploader.toLowerCase().includes(keyword);

            const matchesType =
                selectedType === "all" ||
                getTypeGroup(file.extension) === selectedType;

            return matchesKeyword && matchesType;
        });

        renderFiles(filteredFiles);
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

    function getFileIcon(extension) {
        const ext = extension.toUpperCase();

        if (ext.length > 4) {
            return ext.slice(0, 4);
        }

        return ext;
    }

    function escapeHtml(value) {
        return String(value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }
});