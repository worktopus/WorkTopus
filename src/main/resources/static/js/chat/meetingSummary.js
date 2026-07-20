(function (app) {
    "use strict";


    /* =====================================================
       API
    ===================================================== */

    const MEETING_SUMMARY_API =
        "/api/ai/meeting-summary";

    /*
     * 현재 팝업에 표시 중인
     * AI 회의요약 결과
     */
    let latestSummary = null;

    /*
     * 회의록 기록에서 열어 본
     * 저장된 회의록 PK
     */
    let currentStoredSummaryId = null;


    /* =====================================================
       초기화
    ===================================================== */

    function initializeMeetingSummary() {

        createSummaryModal();

        bindSummaryButton();

        bindSummaryModalEvents();

        bindSummarySaveButton();

        /*
         * 저장된 회의록을
         * 게시글 작성 화면으로 보내는 버튼
         */
        bindSummaryBoardButton();

        /*
         * 채팅창 하단의 일반 게시글 작성 버튼
         */
        bindBoardWriteButton();
    }


    /* =====================================================
       회의록 저장 버튼 연결
    ===================================================== */

    function bindSummarySaveButton() {

        const button =
            document.getElementById(
                "aiSummarySaveButton"
            );


        if (!button) {
            return;
        }


        if (
            button.dataset.eventBound ===
            "true"
        ) {
            return;
        }


        button.dataset.eventBound =
            "true";


        button.addEventListener(
            "click",
            saveMeetingSummary
        );
    }


    /* =====================================================
       저장된 회의록 게시판 등록 버튼 연결
    ===================================================== */

    function bindSummaryBoardButton() {

        const button =
            document.getElementById(
                "aiSummaryBoardButton"
            );


        if (!button) {
            return;
        }


        if (
            button.dataset.eventBound ===
            "true"
        ) {
            return;
        }


        button.dataset.eventBound =
            "true";


        button.addEventListener(
            "click",
            openBoardWriteFromStoredSummary
        );
    }


    /* =====================================================
       채팅창 하단 일반 게시글 작성 버튼 연결
    ===================================================== */

    function bindBoardWriteButton() {

        const button =
            document.getElementById(
                "boardBtn"
            );


        if (!button) {
            return;
        }


        if (
            button.dataset.eventBound ===
            "true"
        ) {
            return;
        }


        button.dataset.eventBound =
            "true";


        button.addEventListener(
            "click",
            openBlankBoardWrite
        );
    }


    /* =====================================================
       저장된 회의록을 게시글 작성 화면으로 전달
    ===================================================== */

    function openBoardWriteFromStoredSummary() {

        const projectId =
            Number(
                latestSummary?.projectId
            );


        const summaryId =
            Number(
                currentStoredSummaryId
            );


        if (
            !Number.isFinite(projectId) ||
            projectId <= 0
        ) {

            alert(
                "회의록의 프로젝트 정보를 확인할 수 없습니다."
            );

            return;
        }


        if (
            !Number.isFinite(summaryId) ||
            summaryId <= 0
        ) {

            alert(
                "저장된 회의록 정보를 확인할 수 없습니다."
            );

            return;
        }


        /*
         * 예:
         * /projects/5/boards/write?summaryId=12
         */
        window.location.href =
            `/projects/${encodeURIComponent(projectId)}` +
            `/boards/write` +
            `?summaryId=${encodeURIComponent(summaryId)}`;
    }


    /* =====================================================
       현재 프로젝트의 빈 게시글 작성 화면으로 이동
    ===================================================== */

    function openBlankBoardWrite() {

        const projectId =
            Number(
                app.state?.currentProjectId
            );


        if (
            !Number.isFinite(projectId) ||
            projectId <= 0
        ) {

            alert(
                "현재 프로젝트를 확인할 수 없습니다."
            );

            return;
        }


        window.location.href =
            `/projects/${encodeURIComponent(projectId)}` +
            `/boards/write`;
    }


    /* =====================================================
       AI 회의요약 버튼 연결
    ===================================================== */

    function bindSummaryButton() {

        const button =
            document.getElementById(
                "aiMeetingSummaryButton"
            );


        if (!button) {

            console.warn(
                "AI 회의요약 버튼을 찾을 수 없습니다."
            );

            return;
        }


        /*
         * 중복 이벤트 등록 방지
         */
        if (
            button.dataset.meetingSummaryBound ===
            "true"
        ) {
            return;
        }


        button.dataset.meetingSummaryBound =
            "true";


        button.addEventListener(
            "click",
            requestMeetingSummary
        );
    }


    /* =====================================================
       AI 회의요약 요청
    ===================================================== */

    async function requestMeetingSummary() {

        /*
         * 현재 선택된 프로젝트 ID
         */
        const projectId =
            Number(
                app.state?.currentProjectId
            );


        /*
         * 현재 채팅방 ID
         */
        const roomId =
            String(
                app.state?.currentRoomId ??
                ""
            );


        if (
            !Number.isFinite(projectId) ||
            projectId <= 0
        ) {

            alert(
                "현재 프로젝트를 확인할 수 없습니다."
            );

            return;
        }


        /*
         * AI 회의요약은
         * 프로젝트 단체채팅에서만 실행
         */
        if (
            app.state?.chatMode !==
            "group" ||
            !roomId.endsWith(
                "_group"
            )
        ) {

            alert(
                "AI 회의요약은 프로젝트 단체채팅에서만 사용할 수 있습니다."
            );

            return;
        }


        setSummaryButtonLoading(
            true
        );


        showSummaryLoading();


        try {

            const response =
                await fetch(
                    `${MEETING_SUMMARY_API}/${encodeURIComponent(projectId)}`,
                    {
                        method:
                            "POST",

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


            showSummaryResult(
                summary
            );


        } catch (error) {

            console.error(
                "AI 회의요약 오류",
                error
            );


            showSummaryError(
                error.message ||
                "AI 회의요약을 생성하지 못했습니다."
            );


        } finally {

            setSummaryButtonLoading(
                false
            );
        }
    }


    /* =====================================================
       결과 팝업 생성
    ===================================================== */

    function createSummaryModal() {

        /*
         * 이미 만들어졌으면 다시 만들지 않음
         */
        if (
            document.getElementById(
                "aiSummaryOverlay"
            )
        ) {
            return;
        }


        const overlay =
            document.createElement(
                "div"
            );


        overlay.id =
            "aiSummaryOverlay";


        overlay.className =
            "ai-summary-overlay";


        overlay.style.display =
            "none";


        overlay.innerHTML = `

            <section
                class="ai-summary-modal"
                role="dialog"
                aria-modal="true"
                aria-labelledby="aiSummaryTitle"
            >

                <header
                    class="ai-summary-modal__header"
                >

                    <div>

                        <p
                            class="ai-summary-modal__eyebrow"
                        >
                            WorkTopus AI
                        </p>

                        <h2
                            id="aiSummaryTitle"
                            class="ai-summary-modal__title"
                        >
                            AI 회의요약
                        </h2>

                    </div>


                    <button
                        type="button"
                        id="aiSummaryCloseButton"
                        class="ai-summary-modal__close"
                        aria-label="AI 회의요약 닫기"
                    >
                        ×
                    </button>

                </header>


                <!-- 로딩 -->
                <div
                    id="aiSummaryLoading"
                    class="ai-summary-loading"
                >

                    <div
                        class="ai-summary-loading__spinner"
                    ></div>

                    <strong>
                        Gemini가 회의 내용을 분석하고 있습니다.
                    </strong>

                    <p>
                        채팅 내용에 따라 잠시 시간이 걸릴 수 있습니다.
                    </p>

                </div>


                <!-- 오류 -->
                <div
                    id="aiSummaryError"
                    class="ai-summary-error"
                    style="display: none;"
                >

                    <strong>
                        회의요약을 생성하지 못했습니다.
                    </strong>

                    <p
                        id="aiSummaryErrorMessage"
                    ></p>

                </div>


                <!-- 실제 결과 -->
                <div
                    id="aiSummaryContent"
                    class="ai-summary-content"
                    style="display: none;"
                >

                    <!-- 전체 요약 -->
                    <section
                        class="ai-summary-section"
                    >

                        <h3>
                            📝 회의 요약
                        </h3>

                        <p
                            id="aiSummaryText"
                            class="ai-summary-text"
                        ></p>

                    </section>


                    <!-- 결정 사항 -->
                    <section
                        class="ai-summary-section"
                    >

                        <h3>
                            ✅ 주요 결정 사항
                        </h3>

                        <ul
                            id="aiDecisionList"
                            class="ai-summary-list"
                        ></ul>

                    </section>


                    <!-- 할 일 -->
                    <section
                        class="ai-summary-section"
                    >

                        <h3>
                            📌 해야 할 일
                        </h3>

                        <ul
                            id="aiActionItemList"
                            class="ai-summary-list"
                        ></ul>

                    </section>


                    <!-- 키워드 -->
                    <section
                        class="ai-summary-section"
                    >

                        <h3>
                            🔑 중요 키워드
                        </h3>

                        <div
                            id="aiKeywordList"
                            class="ai-summary-keywords"
                        ></div>

                    </section>


                    <!-- 요약 정보 -->
                    <footer
                        id="aiSummaryMeta"
                        class="ai-summary-meta"
                    ></footer>


                    <div
                        class="ai-summary-actions"
                    >

                        <button
                            type="button"
                            id="aiSummarySaveButton"
                            class="ai-summary-save-button"
                            disabled
                        >
                            회의록 저장
                        </button>


                        <!-- 저장된 회의록에서만 표시 -->
                        <button
                            type="button"
                            id="aiSummaryBoardButton"
                            class="ai-summary-save-button"
                            style="display: none;"
                        >
                            게시판 등록
                        </button>

                    </div>

                </div>

            </section>
        `;


        document.body.appendChild(
            overlay
        );
    }


    /* =====================================================
       팝업 이벤트
    ===================================================== */

    function bindSummaryModalEvents() {

        const overlay =
            document.getElementById(
                "aiSummaryOverlay"
            );


        const closeButton =
            document.getElementById(
                "aiSummaryCloseButton"
            );


        if (
            closeButton &&
            closeButton.dataset.eventBound !==
            "true"
        ) {

            closeButton.dataset.eventBound =
                "true";


            closeButton.addEventListener(
                "click",
                closeSummaryModal
            );
        }


        /*
         * 팝업 바깥 클릭 시 닫기
         */
        if (
            overlay &&
            overlay.dataset.eventBound !==
            "true"
        ) {

            overlay.dataset.eventBound =
                "true";


            overlay.addEventListener(
                "click",
                function (event) {

                    if (
                        event.target ===
                        overlay
                    ) {
                        closeSummaryModal();
                    }
                }
            );
        }


        /*
         * ESC 키로 닫기
         */
        if (
            document.body.dataset.aiSummaryEscapeBound !==
            "true"
        ) {

            document.body.dataset.aiSummaryEscapeBound =
                "true";


            document.addEventListener(
                "keydown",
                function (event) {

                    if (
                        event.key ===
                        "Escape"
                    ) {
                        closeSummaryModal();
                    }
                }
            );
        }
    }


    /* =====================================================
       로딩 화면
    ===================================================== */

    function showSummaryLoading() {

        /*
         * 새 AI 요약이므로
         * 이전 저장 회의록 정보 제거
         */
        latestSummary = null;

        currentStoredSummaryId = null;


        /*
         * 새 요약 결과에서는
         * 게시판 등록 버튼 숨김
         */
        setSummaryBoardButtonVisible(
            false
        );


        openSummaryModal();


        const saveButton =
            document.getElementById(
                "aiSummarySaveButton"
            );


        if (saveButton) {

            saveButton.disabled =
                true;

            saveButton.textContent =
                "회의록 저장";
        }


        const loading =
            document.getElementById(
                "aiSummaryLoading"
            );


        const content =
            document.getElementById(
                "aiSummaryContent"
            );


        const error =
            document.getElementById(
                "aiSummaryError"
            );


        if (loading) {
            loading.style.display =
                "flex";
        }


        if (content) {
            content.style.display =
                "none";
        }


        if (error) {
            error.style.display =
                "none";
        }
    }


    /* =====================================================
       AI 결과 출력
    ===================================================== */

    function showSummaryResult(
        summary
    ) {

        latestSummary =
            summary;


        const saveButton =
            document.getElementById(
                "aiSummarySaveButton"
            );


        if (saveButton) {

            saveButton.disabled =
                false;

            saveButton.textContent =
                "회의록 저장";
        }


        const loading =
            document.getElementById(
                "aiSummaryLoading"
            );


        const content =
            document.getElementById(
                "aiSummaryContent"
            );


        const error =
            document.getElementById(
                "aiSummaryError"
            );


        if (loading) {
            loading.style.display =
                "none";
        }


        if (error) {
            error.style.display =
                "none";
        }


        if (content) {
            content.style.display =
                "block";
        }


        /*
         * 전체 요약
         */
        setText(
            "aiSummaryText",
            summary.summary ||
            "생성된 요약 내용이 없습니다."
        );


        /*
         * 결정 사항
         */
        renderStringList(
            "aiDecisionList",
            summary.decisions,
            "확정된 결정 사항이 없습니다."
        );


        /*
         * 해야 할 일
         */
        renderStringList(
            "aiActionItemList",
            summary.actionItems,
            "확인된 할 일이 없습니다."
        );


        /*
         * 키워드
         */
        renderKeywords(
            summary.keywords
        );


        /*
         * 하단 정보
         */
        renderSummaryMeta(
            summary
        );
    }


    /* =====================================================
       저장된 회의록 상세 표시
    ===================================================== */

    function showStoredSummary(
        summary,
        summaryId
    ) {

        /*
         * 회의록 기록에서 선택한
         * 저장된 회의록 PK 보관
         */
        currentStoredSummaryId =
            Number(
                summaryId
            );


        openSummaryModal();


        showSummaryResult(
            summary
        );


        /*
         * 이미 DB에 저장된 회의록이므로
         * 다시 저장하지 못하게 함
         */
        const saveButton =
            document.getElementById(
                "aiSummarySaveButton"
            );


        if (saveButton) {

            saveButton.disabled =
                true;

            saveButton.textContent =
                "저장된 회의록";
        }


        /*
         * 저장된 회의록 상세에서만
         * 게시판 등록 버튼 표시
         */
        setSummaryBoardButtonVisible(
            true
        );
    }


    /* =====================================================
       게시판 등록 버튼 표시 여부
    ===================================================== */

    function setSummaryBoardButtonVisible(
        visible
    ) {

        const button =
            document.getElementById(
                "aiSummaryBoardButton"
            );


        if (!button) {
            return;
        }


        button.style.display =
            visible
                ? "inline-flex"
                : "none";
    }


    /* =====================================================
       문자열 목록 출력
    ===================================================== */

    function renderStringList(
        elementId,
        items,
        emptyMessage
    ) {

        const list =
            document.getElementById(
                elementId
            );


        if (!list) {
            return;
        }


        list.replaceChildren();


        const values =
            Array.isArray(items)
                ? items
                : [];


        if (
            values.length ===
            0
        ) {

            const item =
                document.createElement(
                    "li"
                );


            item.className =
                "ai-summary-list__empty";


            item.textContent =
                emptyMessage;


            list.appendChild(
                item
            );

            return;
        }


        values.forEach(
            function (value) {

                const item =
                    document.createElement(
                        "li"
                    );


                item.textContent =
                    String(
                        value ?? ""
                    );


                list.appendChild(
                    item
                );
            }
        );
    }


    /* =====================================================
       키워드 출력
    ===================================================== */

    function renderKeywords(
        keywords
    ) {

        const container =
            document.getElementById(
                "aiKeywordList"
            );


        if (!container) {
            return;
        }


        container.replaceChildren();


        const values =
            Array.isArray(keywords)
                ? keywords
                : [];


        if (
            values.length ===
            0
        ) {

            const empty =
                document.createElement(
                    "span"
                );


            empty.className =
                "ai-summary-keyword-empty";


            empty.textContent =
                "추출된 키워드가 없습니다.";


            container.appendChild(
                empty
            );

            return;
        }


        values.forEach(
            function (keyword) {

                const tag =
                    document.createElement(
                        "span"
                    );


                tag.className =
                    "ai-summary-keyword";


                tag.textContent =
                    String(
                        keyword ?? ""
                    );


                container.appendChild(
                    tag
                );
            }
        );
    }


    /* =====================================================
       AI 회의록 DB 저장
    ===================================================== */

    async function saveMeetingSummary() {

        if (!latestSummary) {

            alert(
                "저장할 AI 회의요약이 없습니다."
            );

            return;
        }


        const button =
            document.getElementById(
                "aiSummarySaveButton"
            );


        /*
         * 중복 클릭 방지
         */
        if (button) {

            button.disabled =
                true;

            button.textContent =
                "저장 중...";
        }


        try {

            const response =
                await fetch(
                    `${MEETING_SUMMARY_API}/save`,
                    {
                        method:
                            "POST",

                        headers: {

                            "Content-Type":
                                "application/json",

                            "Accept":
                                "application/json"
                        },

                        body:
                            JSON.stringify(
                                latestSummary
                            )
                    }
                );


            const responseText =
                await response.text();


            if (!response.ok) {

                throw new Error(
                    getErrorMessage(
                        responseText,
                        response.status
                    )
                );
            }


            const savedResult =
                JSON.parse(
                    responseText
                );


            console.log(
                "AI 회의록 저장 완료:",
                savedResult
            );


            if (button) {

                button.textContent =
                    "저장 완료";

                /*
                 * 같은 AI 결과 중복 저장 방지
                 */
                button.disabled =
                    true;
            }


            alert(
                savedResult.message ||
                "AI 회의록이 저장되었습니다."
            );


        } catch (error) {

            console.error(
                "AI 회의록 저장 오류",
                error
            );


            alert(
                error.message ||
                "AI 회의록을 저장하지 못했습니다."
            );


            /*
             * 저장 실패 시 재시도 가능
             */
            if (button) {

                button.disabled =
                    false;

                button.textContent =
                    "회의록 저장";
            }
        }
    }


    /* =====================================================
       요약 정보 출력
    ===================================================== */

    function renderSummaryMeta(
        summary
    ) {

        const meta =
            document.getElementById(
                "aiSummaryMeta"
            );


        if (!meta) {
            return;
        }


        const messageCount =
            Number(
                summary.messageCount ??
                0
            );


        const generatedAt =
            formatGeneratedAt(
                summary.generatedAt
            );


        meta.textContent =
            `분석 메시지 ${messageCount}개`
            +
            (
                generatedAt
                    ? ` · ${generatedAt}`
                    : ""
            );
    }


    /* =====================================================
       오류 화면
    ===================================================== */

    function showSummaryError(
        message
    ) {

        openSummaryModal();


        const loading =
            document.getElementById(
                "aiSummaryLoading"
            );


        const content =
            document.getElementById(
                "aiSummaryContent"
            );


        const error =
            document.getElementById(
                "aiSummaryError"
            );


        if (loading) {
            loading.style.display =
                "none";
        }


        if (content) {
            content.style.display =
                "none";
        }


        if (error) {
            error.style.display =
                "block";
        }


        setText(
            "aiSummaryErrorMessage",
            message
        );
    }


    /* =====================================================
       팝업 열기
    ===================================================== */

    function openSummaryModal() {

        const overlay =
            document.getElementById(
                "aiSummaryOverlay"
            );


        if (!overlay) {
            return;
        }


        overlay.style.display =
            "flex";
    }


    /* =====================================================
       팝업 닫기
    ===================================================== */

    function closeSummaryModal() {

        const overlay =
            document.getElementById(
                "aiSummaryOverlay"
            );


        if (!overlay) {
            return;
        }


        overlay.style.display =
            "none";
    }


    /* =====================================================
       버튼 로딩 상태
    ===================================================== */

    function setSummaryButtonLoading(
        loading
    ) {

        const button =
            document.getElementById(
                "aiMeetingSummaryButton"
            );


        if (!button) {
            return;
        }


        if (
            !button.dataset.originalText
        ) {

            button.dataset.originalText =
                button.textContent.trim();
        }


        button.disabled =
            loading;


        button.textContent =
            loading
                ? "AI 분석 중..."
                : button.dataset.originalText;
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
                "AI 회의요약 요청에 실패했습니다." +
                ` HTTP ${status}`
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


    /* =====================================================
       공통 텍스트 출력
    ===================================================== */

    function setText(
        elementId,
        value
    ) {

        const element =
            document.getElementById(
                elementId
            );


        if (!element) {
            return;
        }


        element.textContent =
            String(
                value ?? ""
            );
    }


    /* =====================================================
       날짜 표시
    ===================================================== */

    function formatGeneratedAt(
        value
    ) {

        if (!value) {
            return "";
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
            return "";
        }


        return date.toLocaleString(
            "ko-KR"
        );
    }


    /* =====================================================
       외부에서 사용할 함수
    ===================================================== */

    app.meetingSummary = {

        requestMeetingSummary,

        openSummaryModal,

        closeSummaryModal,

        showStoredSummary
    };


    /*
     * 기존 onclick 방식에서도 사용할 수 있도록
     * 전역 함수 제공
     */
    window.requestMeetingSummary =
        requestMeetingSummary;


    /* =====================================================
       페이지 로드 후 실행
    ===================================================== */

    if (
        document.readyState ===
        "loading"
    ) {

        document.addEventListener(
            "DOMContentLoaded",
            initializeMeetingSummary
        );


    } else {

        initializeMeetingSummary();
    }


})(
    window.WorkTopusChat =
        window.WorkTopusChat || {}
);