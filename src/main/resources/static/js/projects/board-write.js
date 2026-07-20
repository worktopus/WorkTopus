document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".board_write_form");
    const content = document.querySelector("[data-editor-content]");
    const contentInput = document.getElementById("contentInput");

/* 2026.07.20 승민 수정  */
    const categoryInput =
        document.getElementById(
            "category"
        );

    const tagInput =
        document.getElementById(
            "tag"
        );

    const titleInput =
        document.getElementById(
            "title"
        );
  /*  여기까지  */

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

    /* URL에 summaryId가 있으면
      저장된 AI 회의록을 게시글 작성 폼에 입력합니다. (승민 수정 2026.07.20)
    */
    loadMeetingSummaryDraft();

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

    /* =====================================================
   URL의 summaryId를 이용한 AI 회의록 불러오기
===================================================== */

    async function loadMeetingSummaryDraft() {

        const searchParams =
            new URLSearchParams(
                window.location.search
            );

        const summaryId =
            Number(
                searchParams.get(
                    "summaryId"
                )
            );

        /*
         * summaryId가 없으면
         * 일반 게시글 작성 화면입니다.
         */
        if (
            !Number.isFinite(summaryId) ||
            summaryId <= 0
        ) {
            return;
        }

        const projectId =
            Number(
                form.dataset.projectId
            );

        if (
            !Number.isFinite(projectId) ||
            projectId <= 0
        ) {
            alert(
                "현재 프로젝트 정보를 확인할 수 없습니다."
            );

            return;
        }

        try {

            const response =
                await fetch(
                    `/api/ai/meeting-summary/detail/` +
                    encodeURIComponent(summaryId),
                    {
                        method:
                            "GET",

                        headers: {
                            "Accept":
                                "application/json"
                        }
                    }
                );

            const responseText =
                await response.text();

            if (!response.ok) {
                throw new Error(
                    getMeetingSummaryErrorMessage(
                        responseText,
                        response.status
                    )
                );
            }

            const summary =
                JSON.parse(
                    responseText
                );

            /*
             * 다른 프로젝트 회의록을
             * 현재 게시판에 넣지 못하도록 검사
             */
            if (
                Number(summary.projectId) !==
                projectId
            ) {
                throw new Error(
                    "현재 프로젝트와 회의록의 프로젝트가 일치하지 않습니다."
                );
            }

            applyMeetingSummaryToForm(
                summary
            );

        } catch (error) {

            console.error(
                "게시글 작성용 회의록 조회 오류",
                error
            );

            alert(
                error.message ||
                "회의록 내용을 불러오지 못했습니다."
            );
        }
    }


    /* =====================================================
       AI 회의록을 게시글 작성 폼에 입력
    ===================================================== */

    function applyMeetingSummaryToForm(
        summary
    ) {

        if (categoryInput) {
            categoryInput.value =
                "FREE";
        }

        if (tagInput) {
            tagInput.value =
                buildMeetingSummaryTag(
                    summary.keywords
                );
        }

        if (titleInput) {
            titleInput.value =
                `[AI 회의록] ${formatMeetingSummaryDate(
                    summary.generatedAt
                )}`;
        }

        const summaryContent =
            buildMeetingSummaryContent(
                summary
            );

        content.replaceChildren(
            summaryContent
        );

        /*
         * 실제 폼 전송용 hidden input에도
         * 같은 내용을 넣습니다.
         */
        contentInput.value =
            content.innerHTML;
    }


    /* =====================================================
       AI 회의록 게시글 본문 생성
    ===================================================== */

    function buildMeetingSummaryContent(
        summary
    ) {

        const wrapper =
            document.createElement(
                "div"
            );

        appendHeading(
            wrapper,
            "AI 회의록",
            "h2"
        );

        appendTextSection(
            wrapper,
            "회의 요약",
            summary.summary ||
            "회의 요약 내용이 없습니다."
        );

        appendListSection(
            wrapper,
            "주요 결정 사항",
            summary.decisions,
            "확정된 결정 사항이 없습니다."
        );

        appendListSection(
            wrapper,
            "해야 할 일",
            summary.actionItems,
            "확인된 할 일이 없습니다."
        );

        appendListSection(
            wrapper,
            "중요 키워드",
            summary.keywords,
            "추출된 키워드가 없습니다."
        );

        const divider =
            document.createElement(
                "hr"
            );

        wrapper.appendChild(
            divider
        );

        const meta =
            document.createElement(
                "p"
            );

        meta.textContent =
            `분석 메시지 ${Number(
                summary.messageCount ?? 0
            )}개`;

        wrapper.appendChild(
            meta
        );

        return wrapper;
    }


    /* =====================================================
       본문 제목 추가
    ===================================================== */

    function appendHeading(
        wrapper,
        text,
        tagName
    ) {

        const heading =
            document.createElement(
                tagName
            );

        heading.textContent =
            text;

        wrapper.appendChild(
            heading
        );
    }


    /* =====================================================
       일반 텍스트 구역 추가
    ===================================================== */

    function appendTextSection(
        wrapper,
        title,
        value
    ) {

        appendHeading(
            wrapper,
            title,
            "h3"
        );

        const paragraph =
            document.createElement(
                "p"
            );

        paragraph.textContent =
            String(
                value ?? ""
            );

        wrapper.appendChild(
            paragraph
        );
    }


    /* =====================================================
       목록 구역 추가
    ===================================================== */

    function appendListSection(
        wrapper,
        title,
        values,
        emptyMessage
    ) {

        appendHeading(
            wrapper,
            title,
            "h3"
        );

        const list =
            document.createElement(
                "ul"
            );

        const items =
            Array.isArray(values)
                ? values.filter(
                    function (value) {
                        return (
                            value != null &&
                            String(value).trim()
                        );
                    }
                )
                : [];

        if (items.length === 0) {

            const item =
                document.createElement(
                    "li"
                );

            item.textContent =
                emptyMessage;

            list.appendChild(
                item
            );

        } else {

            items.forEach(
                function (value) {

                    const item =
                        document.createElement(
                            "li"
                        );

                    /*
                     * textContent 사용으로
                     * AI 문자열의 HTML 실행 방지
                     */
                    item.textContent =
                        String(value);

                    list.appendChild(
                        item
                    );
                }
            );
        }

        wrapper.appendChild(
            list
        );
    }


    /* =====================================================
       태그 생성
    ===================================================== */

    function buildMeetingSummaryTag(
        keywords
    ) {

        const keywordText =
            Array.isArray(keywords)
                ? keywords
                    .filter(
                        function (keyword) {
                            return (
                                keyword != null &&
                                String(keyword).trim()
                            );
                        }
                    )
                    .map(
                        function (keyword) {
                            return String(
                                keyword
                            ).trim();
                        }
                    )
                    .slice(0, 8)
                    .join(",")
                : "";

        const tag =
            keywordText
                ? `AI회의록,회의,${keywordText}`
                : "AI회의록,회의";

        return tag.length <= 200
            ? tag
            : tag.substring(
                0,
                200
            );
    }


    /* =====================================================
       게시글 제목용 날짜
    ===================================================== */

    function formatMeetingSummaryDate(
        value
    ) {

        const date =
            value
                ? new Date(value)
                : new Date();

        if (
            Number.isNaN(
                date.getTime()
            )
        ) {
            return "회의";
        }

        const year =
            date.getFullYear();

        const month =
            String(
                date.getMonth() + 1
            ).padStart(
                2,
                "0"
            );

        const day =
            String(
                date.getDate()
            ).padStart(
                2,
                "0"
            );

        const hour =
            String(
                date.getHours()
            ).padStart(
                2,
                "0"
            );

        const minute =
            String(
                date.getMinutes()
            ).padStart(
                2,
                "0"
            );

        return (
            `${year}-${month}-${day} ` +
            `${hour}:${minute}`
        );
    }


    /* =====================================================
       회의록 API 오류 메시지
    ===================================================== */

    function getMeetingSummaryErrorMessage(
        responseText,
        status
    ) {

        if (!responseText) {
            return (
                `회의록 요청에 실패했습니다. HTTP ${status}`
            );
        }

        try {

            const data =
                JSON.parse(
                    responseText
                );

            return (
                data.message ||
                data.error ||
                responseText
            );

        } catch (error) {

            return responseText;
        }
    }

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
