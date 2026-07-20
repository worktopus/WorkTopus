(function (app) {
    "use strict";


    /* =====================================================
       API
    ===================================================== */

    const MEETING_SUMMARY_API =
        "/api/ai/meeting-summary";


    /* =====================================================
       초기화
    ===================================================== */

    function initializeMeetingHistory() {

        createHistoryModal();

        bindHistoryButton();

        bindHistoryModalEvents();
    }


    /* =====================================================
       회의록 기록 버튼 연결
    ===================================================== */

    function bindHistoryButton() {

        const button =
            document.getElementById(
                "aiMeetingHistoryButton"
            );


        if (!button) {

            console.warn(
                "회의록 기록 버튼을 찾을 수 없습니다."
            );

            return;
        }


        /*
         * 중복 이벤트 등록 방지
         */
        if (
            button.dataset
                .meetingHistoryBound ===
            "true"
        ) {

            return;
        }


        button.dataset
            .meetingHistoryBound =
            "true";


        button.addEventListener(
            "click",
            openMeetingHistory
        );
    }


    /* =====================================================
       회의록 기록 열기
    ===================================================== */

    async function openMeetingHistory() {

        /*
         * 현재 프로젝트 ID
         */
        const projectId =
            Number(
                app.state
                    ?.currentProjectId
            );


        /*
         * 프로젝트 확인
         */
        if (
            !Number.isFinite(
                projectId
            )
            ||
            projectId <= 0
        ) {

            alert(
                "현재 프로젝트를 확인할 수 없습니다."
            );

            return;
        }


        /*
         * 단체채팅에서만 사용
         */
        if (
            app.state?.chatMode !==
            "group"
        ) {

            alert(
                "회의록 기록은 프로젝트 단체채팅에서 확인할 수 있습니다."
            );

            return;
        }


        /*
         * 팝업 열기
         */
        openHistoryModal();


        /*
         * 로딩 화면
         */
        showHistoryLoading();


        try {

            /*
             * 프로젝트별 저장된 회의록 목록 조회
             *
             * GET
             * /api/ai/meeting-summary/project/2
             */
            const response =
                await fetch(
                    `${MEETING_SUMMARY_API}/project/${encodeURIComponent(projectId)}`,
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


            if (
                !response.ok
            ) {

                throw new Error(
                    getErrorMessage(
                        responseText,
                        response.status
                    )
                );
            }


            const summaries =
                JSON.parse(
                    responseText
                );


            /*
             * 목록 출력
             */
            renderHistoryList(
                summaries
            );


        } catch (error) {

            console.error(
                "AI 회의록 목록 조회 오류",
                error
            );


            showHistoryError(
                error.message
                ||
                "회의록 기록을 불러오지 못했습니다."
            );
        }
    }


    /* =====================================================
       회의록 목록 팝업 생성
    ===================================================== */

    function createHistoryModal() {

        /*
         * 이미 있으면 다시 만들지 않음
         */
        if (
            document.getElementById(
                "aiMeetingHistoryOverlay"
            )
        ) {

            return;
        }


        const overlay =
            document.createElement(
                "div"
            );


        overlay.id =
            "aiMeetingHistoryOverlay";


        overlay.className =
            "ai-history-overlay";


        overlay.style.display =
            "none";


        overlay.innerHTML = `

            <section
                class="ai-history-modal"
                role="dialog"
                aria-modal="true"
                aria-labelledby="aiMeetingHistoryTitle"
            >

                <header
                    class="ai-history-modal__header"
                >

                    <div>

                        <p
                            class="ai-history-modal__eyebrow"
                        >
                            WorkTopus AI
                        </p>

                        <h2
                            id="aiMeetingHistoryTitle"
                            class="ai-history-modal__title"
                        >
                            회의록 기록
                        </h2>

                    </div>


                    <button
                        type="button"
                        id="aiMeetingHistoryCloseButton"
                        class="ai-history-modal__close"
                        aria-label="회의록 기록 닫기"
                    >
                        ×
                    </button>

                </header>


                <!-- 로딩 -->

                <div
                    id="aiMeetingHistoryLoading"
                    class="ai-history-loading"
                >

                    <div
                        class="ai-history-loading__spinner"
                    ></div>

                    <p>
                        저장된 회의록을 불러오고 있습니다.
                    </p>

                </div>


                <!-- 빈 목록 -->

                <div
                    id="aiMeetingHistoryEmpty"
                    class="ai-history-empty"
                    style="display: none;"
                >

                    <strong>
                        저장된 회의록이 없습니다.
                    </strong>

                    <p>
                        AI 회의요약을 생성한 뒤
                        회의록 저장 버튼을 눌러보세요.
                    </p>

                </div>


                <!-- 오류 -->

                <div
                    id="aiMeetingHistoryError"
                    class="ai-history-error"
                    style="display: none;"
                >

                    <strong>
                        회의록을 불러오지 못했습니다.
                    </strong>

                    <p
                        id="aiMeetingHistoryErrorMessage"
                    ></p>

                </div>


                <!-- 회의록 목록 -->

                <div
                    id="aiMeetingHistoryList"
                    class="ai-history-list"
                    style="display: none;"
                ></div>

            </section>
        `;


        document.body.appendChild(
            overlay
        );
    }


    /* =====================================================
       팝업 이벤트
    ===================================================== */

    function bindHistoryModalEvents() {

        const overlay =
            document.getElementById(
                "aiMeetingHistoryOverlay"
            );


        const closeButton =
            document.getElementById(
                "aiMeetingHistoryCloseButton"
            );


        if (
            closeButton
            &&
            closeButton.dataset
                .eventBound !==
            "true"
        ) {

            closeButton.dataset
                .eventBound =
                "true";


            closeButton.addEventListener(
                "click",
                closeHistoryModal
            );
        }


        /*
         * 배경 클릭 시 닫기
         */
        if (
            overlay
            &&
            overlay.dataset
                .eventBound !==
            "true"
        ) {

            overlay.dataset
                .eventBound =
                "true";


            overlay.addEventListener(
                "click",
                function (event) {

                    if (
                        event.target ===
                        overlay
                    ) {

                        closeHistoryModal();
                    }
                }
            );
        }
    }


    /* =====================================================
       로딩 화면
    ===================================================== */

    function showHistoryLoading() {

        setDisplay(
            "aiMeetingHistoryLoading",
            "flex"
        );


        setDisplay(
            "aiMeetingHistoryEmpty",
            "none"
        );


        setDisplay(
            "aiMeetingHistoryError",
            "none"
        );


        setDisplay(
            "aiMeetingHistoryList",
            "none"
        );
    }


    /* =====================================================
       회의록 목록 출력
    ===================================================== */

    function renderHistoryList(
        summaries
    ) {

        const list =
            document.getElementById(
                "aiMeetingHistoryList"
            );


        if (!list) {
            return;
        }


        list.replaceChildren();


        const values =
            Array.isArray(
                summaries
            )
                ? summaries
                : [];


        /*
         * 로딩 숨기기
         */
        setDisplay(
            "aiMeetingHistoryLoading",
            "none"
        );


        setDisplay(
            "aiMeetingHistoryError",
            "none"
        );


        /*
         * 저장된 회의록 없음
         */
        if (
            values.length ===
            0
        ) {

            setDisplay(
                "aiMeetingHistoryEmpty",
                "block"
            );


            setDisplay(
                "aiMeetingHistoryList",
                "none"
            );


            return;
        }


        setDisplay(
            "aiMeetingHistoryEmpty",
            "none"
        );


        setDisplay(
            "aiMeetingHistoryList",
            "block"
        );


        /*
         * 회의록 하나씩 생성
         */
        values.forEach(
            function (summary) {

                const item =
                    createHistoryItem(
                        summary
                    );


                list.appendChild(
                    item
                );
            }
        );
    }


    /* =====================================================
       회의록 목록 한 개 생성
    ===================================================== */

    function createHistoryItem(
        summary
    ) {

        const button =
            document.createElement(
                "button"
            );


        button.type =
            "button";


        button.className =
            "ai-history-item";


        /*
         * summaryId 저장
         */
        button.dataset.summaryId =
            String(
                summary.summaryId
            );


        /*
         * 날짜
         */
        const date =
            document.createElement(
                "span"
            );


        date.className =
            "ai-history-item__date";


        date.textContent =
            formatGeneratedAt(
                summary.generatedAt
            );


        /*
         * 요약 내용
         */
        const content =
            document.createElement(
                "strong"
            );


        content.className =
            "ai-history-item__summary";


        content.textContent =
            summary.summary
            ||
            "회의요약 내용이 없습니다.";


        /*
         * 메시지 개수
         */
        const meta =
            document.createElement(
                "span"
            );


        meta.className =
            "ai-history-item__meta";


        meta.textContent =
            `분석 메시지 ${Number(summary.messageCount ?? 0)}개`;


        button.append(
            date,
            content,
            meta
        );


        /*
         * 목록 클릭
         * →
         * 상세 조회
         */
        button.addEventListener(
            "click",
            function () {

                loadMeetingSummaryDetail(
                    summary.summaryId
                );
            }
        );


        return button;
    }


    /* =====================================================
       저장된 회의록 상세 조회
    ===================================================== */

    async function loadMeetingSummaryDetail(
        summaryId
    ) {

        const id =
            Number(
                summaryId
            );


        if (
            !Number.isFinite(
                id
            )
            ||
            id <= 0
        ) {

            alert(
                "회의록 정보를 확인할 수 없습니다."
            );

            return;
        }


        try {

            /*
             * 상세 조회
             *
             * GET
             * /api/ai/meeting-summary/detail/1
             */
            const response =
                await fetch(
                    `${MEETING_SUMMARY_API}/detail/${encodeURIComponent(id)}`,
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


            if (
                !response.ok
            ) {

                throw new Error(
                    getErrorMessage(
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
             * 목록 팝업 닫기
             */
            closeHistoryModal();


            /*
             * 기존 AI 요약 팝업 재사용
             */
            if (
                !app.meetingSummary
                ||
                typeof app.meetingSummary
                    .showStoredSummary !==
                "function"
            ) {

                throw new Error(
                    "AI 회의요약 화면을 불러올 수 없습니다."
                );
            }


            app.meetingSummary
                .showStoredSummary(
                    summary
                );


        } catch (error) {

            console.error(
                "AI 회의록 상세 조회 오류",
                error
            );


            alert(
                error.message
                ||
                "회의록 상세 내용을 불러오지 못했습니다."
            );
        }
    }


    /* =====================================================
       오류 화면
    ===================================================== */

    function showHistoryError(
        message
    ) {

        setDisplay(
            "aiMeetingHistoryLoading",
            "none"
        );


        setDisplay(
            "aiMeetingHistoryEmpty",
            "none"
        );


        setDisplay(
            "aiMeetingHistoryList",
            "none"
        );


        setDisplay(
            "aiMeetingHistoryError",
            "block"
        );


        const messageElement =
            document.getElementById(
                "aiMeetingHistoryErrorMessage"
            );


        if (messageElement) {

            messageElement.textContent =
                String(
                    message ?? ""
                );
        }
    }


    /* =====================================================
       팝업 열기 / 닫기
    ===================================================== */

    function openHistoryModal() {

        const overlay =
            document.getElementById(
                "aiMeetingHistoryOverlay"
            );


        if (!overlay) {
            return;
        }


        overlay.style.display =
            "flex";
    }


    function closeHistoryModal() {

        const overlay =
            document.getElementById(
                "aiMeetingHistoryOverlay"
            );


        if (!overlay) {
            return;
        }


        overlay.style.display =
            "none";
    }


    /* =====================================================
       공통 display 처리
    ===================================================== */

    function setDisplay(
        elementId,
        display
    ) {

        const element =
            document.getElementById(
                elementId
            );


        if (!element) {
            return;
        }


        element.style.display =
            display;
    }


    /* =====================================================
       날짜 표시
    ===================================================== */

    function formatGeneratedAt(
        value
    ) {

        if (!value) {

            return "생성 시간 없음";
        }


        const date =
            new Date(
                value
            );


        if (
            Number.isNaN(
                date.getTime()
            )
        ) {

            return String(
                value
            );
        }


        return date
            .toLocaleString(
                "ko-KR"
            );
    }


    /* =====================================================
       서버 오류 메시지 처리
    ===================================================== */

    function getErrorMessage(
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
                data.message
                ||
                data.error
                ||
                responseText
            );


        } catch (error) {

            return responseText;
        }
    }


    /* =====================================================
       외부 공개
    ===================================================== */

    app.meetingHistory = {

        openMeetingHistory,

        closeHistoryModal
    };


    /* =====================================================
       페이지 초기화
    ===================================================== */

    if (
        document.readyState ===
        "loading"
    ) {

        document.addEventListener(
            "DOMContentLoaded",
            initializeMeetingHistory
        );

    } else {

        initializeMeetingHistory();
    }


})(
    window.WorkTopusChat =
        window.WorkTopusChat || {}
);