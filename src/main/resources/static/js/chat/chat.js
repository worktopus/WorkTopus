(function (app) {
    "use strict";


    /* =====================================================
       공통 상태 초기화
    ===================================================== */

    app.state = app.state || {};

    window.loginUser =
        window.loginUser || {
            userNum: null,
            userId: "",
            name: ""
        };

    app.state.loginUser =
        app.state.loginUser || {
            userNum:
                normalizeNumber(
                    window.loginUser.userNum
                ),

            userId:
                String(
                    window.loginUser.userId ??
                    ""
                ),

            name:
                String(
                    window.loginUser.name ??
                    window.loginUser.userName ??
                    ""
                )
        };

    const defaultState = {
        currentProjectId: null,
        currentProjectName: "",
        currentRoomId: null,

        currentPrivateUserNum: null,
        currentPrivateMemberName: "",

        chatMode: "group",
        socketConnected: false,
        initialized: false
    };

    Object.entries(defaultState).forEach(
        function ([key, value]) {
            if (
                app.state[key] === undefined
            ) {
                app.state[key] = value;
            }
        }
    );


    /* =====================================================
       채팅 초기화
    ===================================================== */

    async function initializeChat() {
        if (app.state.initialized) {
            return;
        }

        app.state.initialized = true;

        bindChatEvents();
        syncLegacyVariables();
        updateMessageInputState();

        connectWebSocket();

        if (
            app.project &&
            typeof app.project.loadProjects ===
            "function"
        ) {
            await app.project.loadProjects();
        }
    }


    /* =====================================================
       채팅 팝업 열기
    ===================================================== */

    async function openChat() {
        const modal =
            document.getElementById(
                "chatModal"
            );

        if (!modal) {
            console.error(
                "chatModal 요소를 찾을 수 없습니다."
            );

            return;
        }

        modal.style.display =
            "block";

        /*
         * 채팅창을 열 때 WebSocket 연결과
         * 현재 채팅방 구독 상태를 다시 확인합니다.
         */
        ensureRealtimeConnection();

        /*
         * 팝업을 열면 먼저 프로젝트 데이터를 조회합니다.
         */

        if (
            app.project &&
            typeof app.project.loadProjects ===
            "function"
        ) {
            await app.project.loadProjects();
        }

        /*
         * 처음에는 실제 대화창이 아니라
         * 채팅방 목록 화면을 표시합니다.
         */
        if (
            app.chatList &&
            typeof app.chatList.showChatListView ===
            "function"
        ) {
            app.chatList.showChatListView();
        }
    }


    /* =====================================================
       채팅 팝업 닫기
    ===================================================== */

    function closeChat() {
        const modal =
            document.getElementById(
                "chatModal"
            );

        if (!modal) {
            return;
        }

        modal.style.display =
            "none";
    }


    /* =====================================================
       단체 / 개인 채팅 탭 변경
    ===================================================== */

    function switchTab(type) {
        if (type === "group") {
            activateGroupChat();
            return;
        }

        if (type === "private") {
            activatePrivateChat();
        }
    }


    /* =====================================================
       단체 채팅 활성화
    ===================================================== */

    function activateGroupChat() {
        const project =
            getCurrentProject();

        app.state.chatMode =
            "group";

        app.state.currentPrivateUserNum =
            null;

        app.state.currentPrivateMemberName =
            "";

        if (project) {
            app.state.currentProjectId =
                project.id;

            app.state.currentProjectName =
                project.name;

            app.state.currentRoomId =
                project.roomId;

        } else {
            app.state.currentRoomId =
                null;
        }

        changeTabDisplay("group");
        updateChatTitle();
        syncLegacyVariables();
        updateMessageInputState();

        if (!app.state.currentRoomId) {
            showNoProjectMessage();
            return;
        }

        subscribeCurrentRoom();
        loadCurrentRoomHistory();
        focusMessageInput();
    }


    /* =====================================================
       개인 채팅 활성화
    ===================================================== */

    function activatePrivateChat() {
        app.state.chatMode =
            "private";

        changeTabDisplay("private");

        if (
            app.state.currentPrivateUserNum ===
            null
        ) {
            app.state.currentRoomId =
                null;

            renderPrivateChatPlaceholder();

            syncLegacyVariables();
            updateMessageInputState();

            return;
        }

        const project =
            getCurrentProject();

        if (!project) {
            app.state.currentRoomId =
                null;

            renderPrivateChatPlaceholder();

            syncLegacyVariables();
            updateMessageInputState();

            return;
        }

        app.state.currentRoomId =
            createPrivateRoomId(
                project.id,
                app.state.loginUser.userNum,
                app.state.currentPrivateUserNum
            );

        renderPrivateChatRoom(
            app.state.currentPrivateMemberName
        );

        updateChatTitle();
        syncLegacyVariables();
        updateMessageInputState();

        subscribeCurrentRoom();
        loadCurrentRoomHistory();
        focusMessageInput();
    }


    /* =====================================================
       프로젝트 선택
    ===================================================== */

    function selectProject(project) {
        if (!project) {
            return;
        }

        const projectId =
            normalizeNumber(
                project.projectId ??
                project.id
            );

        if (projectId === null) {
            console.error(
                "프로젝트 ID가 올바르지 않습니다.",
                project
            );

            return;
        }

        app.state.currentProjectId =
            projectId;

        app.state.currentProjectName =
            String(
                project.projectName ??
                project.name ??
                ""
            );

        app.state.currentRoomId =
            String(
                project.groupRoomId ??
                project.roomId ??
                createGroupRoomId(
                    projectId
                )
            );

        app.state.currentPrivateUserNum =
            null;

        app.state.currentPrivateMemberName =
            "";

        app.state.chatMode =
            "group";

        changeTabDisplay("group");
        updateChatTitle();
        syncLegacyVariables();
        updateMessageInputState();

        clearGroupMessageArea();

        subscribeCurrentRoom();
        loadCurrentRoomHistory();
    }


    /* =====================================================
       개인 채팅 팀원 선택
    ===================================================== */

    function selectPrivateMember(
        project,
        member
    ) {
        if (!project || !member) {
            return;
        }

        const loginUserNum =
            normalizeNumber(
                app.state.loginUser.userNum
            );

        const memberUserNum =
            normalizeNumber(
                member.userNum ??
                member.memberId ??
                member.id
            );

        const projectId =
            normalizeNumber(
                project.projectId ??
                project.id
            );

        if (
            loginUserNum === null ||
            memberUserNum === null ||
            projectId === null
        ) {
            console.error(
                "개인 채팅 사용자 또는 프로젝트 정보가 올바르지 않습니다."
            );

            return;
        }

        /*
        자기 자신과는 개인 채팅을 열지 않습니다.
        */
        if (
            loginUserNum ===
            memberUserNum
        ) {
            return;
        }

        app.state.currentProjectId =
            projectId;

        app.state.currentProjectName =
            String(
                project.projectName ??
                project.name ??
                ""
            );

        app.state.currentPrivateUserNum =
            memberUserNum;

        app.state.currentPrivateMemberName =
            String(
                member.userName ??
                member.memberName ??
                member.name ??
                ""
            );

        app.state.chatMode =
            "private";

        app.state.currentRoomId =
            createPrivateRoomId(
                projectId,
                loginUserNum,
                memberUserNum
            );

        changeTabDisplay("private");

        renderPrivateChatRoom(
            app.state.currentPrivateMemberName
        );

        updateChatTitle();
        syncLegacyVariables();
        updateMessageInputState();

        subscribeCurrentRoom();
        loadCurrentRoomHistory();
        focusMessageInput();
    }


    /* =====================================================
       기존 selectUser 호출 호환
    ===================================================== */

    function selectUser(
        userNum,
        userName
    ) {
        const project =
            getCurrentProject();

        if (!project) {
            return;
        }

        const normalizedUserNum =
            normalizeNumber(userNum);

        if (
            normalizedUserNum === null
        ) {
            return;
        }

        let member = null;

        if (
            Array.isArray(
                project.members
            )
        ) {
            member =
                project.members.find(
                    function (item) {
                        return (
                            normalizeNumber(
                                item.userNum ??
                                item.memberId ??
                                item.id
                            ) ===
                            normalizedUserNum
                        );
                    }
                ) ?? null;
        }

        if (!member) {
            member = {
                userNum:
                normalizedUserNum,

                name:
                    String(
                        userName ?? ""
                    )
            };
        }

        selectPrivateMember(
            project,
            member
        );
    }


    /* =====================================================
       메시지 전송
    ===================================================== */

    function sendMessage() {
        const input =
            document.getElementById(
                "message"
            );

        if (!input) {
            return;
        }

        const text =
            input.value.trim();

        if (!text) {
            return;
        }

        if (!app.state.currentRoomId) {
            console.warn(
                "선택된 채팅방이 없습니다."
            );

            updateMessageInputState();

            return;
        }

        if (!isWebSocketConnected()) {
            console.warn(
                "WebSocket 연결 전에는 메시지를 전송할 수 없습니다."
            );

            connectWebSocket();

            return;
        }

        /*
        projectId, roomId, 메시지 내용을 서버에 전달합니다.

        senderNum과 senderName은 개발 단계 호환용으로 넣습니다.
        최종 서버에서는 로그인 정보를 기준으로 다시 설정해야 합니다.
        */
        const message = {
            projectId:
            app.state.currentProjectId,

            roomId:
            app.state.currentRoomId,

            senderNum:
            app.state.loginUser.userNum,

            senderName:
            app.state.loginUser.name,

            message:
            text,

            type:
                "TALK",

            createdAt:
                new Date().toISOString()
        };

        const sent =
            sendWebSocketMessage(
                message
            );

        if (!sent) {
            return;
        }

        input.value = "";
        input.focus();
    }


    /* =====================================================
       WebSocket 메시지 수신
    ===================================================== */

    function handleIncomingMessage(
        message
    ) {
        if (!message) {
            return;
        }

        const roomId =
            String(
                message.roomId ??
                ""
            );

        /*
         * 모든 수신 메시지를 채팅방 목록에 반영합니다.
         *
         * 마지막 메시지
         * 시간
         * 안 읽은 메시지 수
         * 목록 정렬
         */
        if (
            app.chatList &&
            typeof app.chatList
                .updateRoomFromMessage ===
            "function"
        ) {
            app.chatList
                .updateRoomFromMessage(
                    message
                );
        }

        /*
         * 프로젝트 단체 채팅의 마지막 메시지를
         * 기존 프로젝트 데이터에도 반영합니다.
         */
        if (
            isGroupRoom(roomId) &&
            app.project &&
            typeof app.project
                .updateProjectLastMessage ===
            "function"
        ) {
            app.project
                .updateProjectLastMessage(
                    message
                );
        }

        /*
         * 현재 열어 본 채팅방이 아니라면
         * 대화 화면에는 출력하지 않습니다.
         *
         * 목록의 마지막 메시지와 알림만 갱신됩니다.
         */
        if (
            roomId !==
            String(
                app.state.currentRoomId ??
                ""
            )
        ) {
            return;
        }

        if (isGroupRoom(roomId)) {
            showGroupMessage(
                message
            );

        } else {
            showPrivateMessage(
                message
            );
        }
    }

    /* =====================================================
       WebSocket 연결 상태 변경
    ===================================================== */

    function handleConnectionChange(
        connected
    ) {
        app.state.socketConnected =
            Boolean(connected);

        updateMessageInputState();

        /*
         * 서버 재시작 또는 네트워크 끊김 후
         * 연결이 복구되면 현재 채팅방을 다시 구독합니다.
         */
        if (
            connected &&
            app.state.currentRoomId
        ) {
            subscribeCurrentRoom();
        }
    }


    /* =====================================================
       WebSocket 연결
    ===================================================== */

    function connectWebSocket() {
        if (
            app.websocket &&
            typeof app.websocket.connect ===
            "function"
        ) {
            app.websocket.connect();
            return;
        }

        if (
            typeof window.connect ===
            "function"
        ) {
            window.connect();
        }
    }

    /* =====================================================
        실시간 연결 및 현재 방 구독 확인
        ===================================================== */

    function ensureRealtimeConnection() {

        /*
         * 연결이 끊어진 경우 다시 연결합니다.
         */
        if (!isWebSocketConnected()) {
            connectWebSocket();
            return;
        }

        /*
         * 이미 연결되어 있으면 현재 채팅방을
         * 다시 구독 상태로 맞춥니다.
         */
        if (app.state.currentRoomId) {
            subscribeCurrentRoom();
        }
    }


    /* =====================================================
       현재 채팅방 구독
    ===================================================== */

    function subscribeCurrentRoom() {
        const roomId =
            app.state.currentRoomId;

        if (!roomId) {
            return;
        }

        if (
            app.websocket &&
            typeof app.websocket
                .subscribeRoom ===
            "function"
        ) {
            app.websocket.subscribeRoom(
                roomId
            );

            return;
        }

        if (
            typeof window.subscribeRoom ===
            "function"
        ) {
            window.subscribeRoom(
                roomId
            );
        }
    }


    /* =====================================================
       WebSocket 메시지 전송
    ===================================================== */

    function sendWebSocketMessage(
        message
    ) {
        if (
            app.websocket &&
            typeof app.websocket.send ===
            "function"
        ) {
            return app.websocket.send(
                message
            );
        }

        if (
            window.stompClient &&
            window.stompClient.connected
        ) {
            window.stompClient.send(
                "/app/chat.send",
                {},
                JSON.stringify(message)
            );

            return true;
        }

        return false;
    }


    /* =====================================================
       WebSocket 연결 여부
    ===================================================== */

    function isWebSocketConnected() {
        if (
            app.websocket &&
            typeof app.websocket
                .isConnected ===
            "function"
        ) {
            return app.websocket
                .isConnected();
        }

        return Boolean(
            window.stompClient &&
            window.stompClient.connected
        );
    }


    /* =====================================================
       현재 채팅 내역 조회
    ===================================================== */

    function loadCurrentRoomHistory() {
        const roomId =
            app.state.currentRoomId;

        if (!roomId) {
            return;
        }

        if (
            app.history &&
            typeof app.history
                .loadHistory ===
            "function"
        ) {
            app.history.loadHistory(
                roomId
            );

            return;
        }

        if (
            typeof window.loadHistory ===
            "function"
        ) {
            window.loadHistory(
                roomId
            );
        }
    }


    /* =====================================================
       단체 메시지 출력
    ===================================================== */

    function showGroupMessage(
        message
    ) {
        if (
            app.groupChat &&
            typeof app.groupChat
                .showMessage ===
            "function"
        ) {
            app.groupChat.showMessage(
                message
            );

            return;
        }

        if (
            typeof window.showGroupMessage ===
            "function"
        ) {
            window.showGroupMessage(
                message
            );
        }
    }


    /* =====================================================
       개인 메시지 출력
    ===================================================== */

    function showPrivateMessage(
        message
    ) {
        if (
            app.privateChat &&
            typeof app.privateChat
                .showMessage ===
            "function"
        ) {
            app.privateChat.showMessage(
                message
            );

            return;
        }

        if (
            typeof window.showPrivateMessage ===
            "function"
        ) {
            window.showPrivateMessage(
                message
            );
        }
    }


    /* =====================================================
       프로젝트 목록 조회
    ===================================================== */

    async function loadProjectList() {
        if (
            !app.project ||
            typeof app.project.loadProjects !==
            "function"
        ) {
            return [];
        }

        try {
            return await app.project
                .loadProjects();

        } catch (error) {
            console.error(
                "프로젝트 목록 조회 실패",
                error
            );

            return [];
        }
    }


    /* =====================================================
       현재 프로젝트 조회
    ===================================================== */

    function getCurrentProject() {
        if (
            app.project &&
            typeof app.project
                .getCurrentProject ===
            "function"
        ) {
            return app.project
                .getCurrentProject();
        }

        return null;
    }


    /* =====================================================
       단체 / 개인 탭 화면 변경
    ===================================================== */

    function changeTabDisplay(type) {

        const groupChat =
            document.getElementById(
                "groupChat"
            );

        const privateChat =
            document.getElementById(
                "privateChat"
            );

        const isGroup =
            type === "group";


        /*
         * block이 아니라 flex로 표시해야
         * 메시지 영역이 남은 높이를 차지하고
         * 내부 스크롤이 정상적으로 작동합니다.
         */
        if (groupChat) {
            groupChat.style.display =
                isGroup
                    ? "flex"
                    : "none";
        }


        if (privateChat) {
            privateChat.style.display =
                isGroup
                    ? "none"
                    : "flex";
        }


        const groupTab =
            document.getElementById(
                "groupTab"
            );

        const privateTab =
            document.getElementById(
                "privateTab"
            );


        if (groupTab) {
            groupTab.classList.toggle(
                "active",
                isGroup
            );
        }


        if (privateTab) {
            privateTab.classList.toggle(
                "active",
                !isGroup
            );
        }


        /*
         * 탭 버튼에 ID가 없는 기존 HTML 구조도 지원합니다.
         */
        const tabButtons =
            document.querySelectorAll(
                ".chat-tabs button"
            );


        if (tabButtons.length >= 2) {

            tabButtons[0]
                .classList.toggle(
                "active",
                isGroup
            );

            tabButtons[1]
                .classList.toggle(
                "active",
                !isGroup
            );
        }


        /*
         * 숨겨져 있던 채팅 영역이 실제로 표시된 다음
         * 가장 최신 메시지 위치로 이동합니다.
         *
         * requestAnimationFrame을 두 번 사용하면
         * 브라우저가 flex 높이를 계산한 뒤 스크롤을 이동합니다.
         */
        requestAnimationFrame(
            function () {

                requestAnimationFrame(
                    function () {

                        if (
                            isGroup &&
                            app.groupChat &&
                            typeof app.groupChat
                                .scrollToBottom ===
                            "function"
                        ) {
                            app.groupChat
                                .scrollToBottom(
                                    false
                                );

                            return;
                        }


                        if (
                            !isGroup &&
                            app.privateChat &&
                            typeof app.privateChat
                                .scrollToBottom ===
                            "function"
                        ) {
                            app.privateChat
                                .scrollToBottom(
                                    false
                                );
                        }
                    }
                );
            }
        );
    }


    /* =====================================================
       개인 채팅 상대방 화면 생성
    ===================================================== */

    function renderPrivateChatRoom(
        memberName
    ) {
        const privateMessageArea =
            document.getElementById(
                "privateMessageArea"
            );

        if (!privateMessageArea) {
            return;
        }

        privateMessageArea.innerHTML = `
            <h3>
                ${escapeHtml(memberName)}
            </h3>

            <hr>

            <div id="dmArea"></div>
        `;
    }


    /* =====================================================
       개인 채팅 팀원 미선택 화면
    ===================================================== */

    function renderPrivateChatPlaceholder() {
        const privateMessageArea =
            document.getElementById(
                "privateMessageArea"
            );

        if (!privateMessageArea) {
            return;
        }

        privateMessageArea.innerHTML = `
            <div class="private-chat-empty">
                팀원을 선택하세요.
            </div>
        `;
    }


    /* =====================================================
       프로젝트 없음 화면
    ===================================================== */

    function showNoProjectMessage() {
        const messageArea =
            document.getElementById(
                "messageArea"
            );

        if (!messageArea) {
            return;
        }

        messageArea.innerHTML = `
            <div class="chat-empty-message">
                참여 중인 프로젝트가 없습니다.
            </div>
        `;
    }


    /* =====================================================
       단체 메시지 화면 초기화
    ===================================================== */

    function clearGroupMessageArea() {
        if (
            app.groupChat &&
            typeof app.groupChat
                .clearMessages ===
            "function"
        ) {
            app.groupChat.clearMessages();
            return;
        }

        const messageArea =
            document.getElementById(
                "messageArea"
            );

        if (messageArea) {
            messageArea.innerHTML =
                "";
        }
    }


    /* =====================================================
       채팅 제목 변경
    ===================================================== */

    function updateChatTitle() {
        const chatTitle =
            document.getElementById(
                "chatTitle"
            );

        if (!chatTitle) {
            return;
        }

        chatTitle.textContent =
            app.state.currentProjectName ||
            "프로젝트 채팅";
    }


    /* =====================================================
       메시지 입력창 상태
    ===================================================== */

    function updateMessageInputState() {
        const input =
            document.getElementById(
                "message"
            );

        const sendButton =
            document.getElementById(
                "sendBtn"
            );

        const hasRoom =
            Boolean(
                app.state.currentRoomId
            );

        const connected =
            isWebSocketConnected();

        const disabled =
            !hasRoom ||
            !connected;

        if (input) {
            input.disabled =
                disabled;

            if (!hasRoom) {
                input.placeholder =
                    "채팅방을 선택하세요.";

            } else if (!connected) {
                input.placeholder =
                    "채팅 서버에 연결 중입니다.";

            } else {
                input.placeholder =
                    "메시지를 입력하세요";
            }
        }

        if (sendButton) {
            sendButton.disabled =
                disabled;
        }
    }


    /* =====================================================
       메시지 입력창 포커스
    ===================================================== */

    function focusMessageInput() {
        const input =
            document.getElementById(
                "message"
            );

        if (
            input &&
            !input.disabled
        ) {
            window.setTimeout(
                function () {
                    input.focus();
                },
                0
            );
        }
    }


    /* =====================================================
       채팅 이벤트 등록
    ===================================================== */

    function bindChatEvents() {
        const sendButton =
            document.getElementById(
                "sendBtn"
            );

        if (
            sendButton &&
            sendButton.dataset
                .chatEventBound !== "true"
        ) {
            sendButton.dataset
                .chatEventBound = "true";

            sendButton.addEventListener(
                "click",
                sendMessage
            );
        }

        const input =
            document.getElementById(
                "message"
            );

        if (
            input &&
            input.dataset
                .chatEventBound !== "true"
        ) {
            input.dataset
                .chatEventBound = "true";

            input.addEventListener(
                "keydown",
                function (event) {
                    if (
                        event.isComposing ||
                        event.keyCode === 229
                    ) {
                        return;
                    }

                    if (
                        event.key === "Enter"
                    ) {
                        event.preventDefault();

                        sendMessage();
                    }
                }
            );
        }
    }


    /* =====================================================
       단체 채팅방 ID 생성
    ===================================================== */

    function createGroupRoomId(
        projectId
    ) {
        return (
            `project_${projectId}_group`
        );
    }


    /* =====================================================
       개인 채팅방 ID 생성

       두 사용자의 userNum을 정렬하여
       어느 사용자가 먼저 선택해도 같은 방이 생성됩니다.
    ===================================================== */

    function createPrivateRoomId(
        projectId,
        firstUserNum,
        secondUserNum
    ) {
        const first =
            normalizeNumber(
                firstUserNum
            );

        const second =
            normalizeNumber(
                secondUserNum
            );

        if (
            first === null ||
            second === null
        ) {
            return "";
        }

        const small =
            Math.min(
                first,
                second
            );

        const large =
            Math.max(
                first,
                second
            );

        return (
            `project_${projectId}` +
            `_private_${small}_${large}`
        );
    }


    /* =====================================================
       단체 채팅방 여부
    ===================================================== */

    function isGroupRoom(roomId) {
        return String(
            roomId ?? ""
        ).endsWith(
            "_group"
        );
    }


    /* =====================================================
       기존 전역 변수 호환
    ===================================================== */

    function syncLegacyVariables() {
        window.chatMode =
            app.state.chatMode;

        window.currentProjectId =
            app.state.currentProjectId;

        window.currentRoom =
            app.state.currentRoomId;

        /*
        기존 코드에서 currentUserId가 숫자 사용자 식별값으로
        사용됐기 때문에 임시로 userNum을 넣습니다.
        */
        window.currentUserId =
            app.state.loginUser.userNum;

        window.currentUserNum =
            app.state.loginUser.userNum;

        window.currentUser =
            app.state.loginUser.name;
    }


    /* =====================================================
       숫자 변환
    ===================================================== */

    function normalizeNumber(
        value
    ) {
        if (
            value === null ||
            value === undefined ||
            value === ""
        ) {
            return null;
        }

        const number =
            Number(value);

        return Number.isFinite(number)
            ? number
            : null;
    }


    /* =====================================================
       HTML 보안 처리
    ===================================================== */

    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }


    /* =====================================================
       외부 파일에서 사용할 함수
    ===================================================== */

    app.chat = {
        initializeChat,

        openChat,
        closeChat,
        switchTab,
        sendMessage,

        selectProject,
        selectPrivateMember,
        selectUser,

        handleIncomingMessage,
        handleConnectionChange,

        createGroupRoomId,
        createPrivateRoomId,

        getCurrentRoomId:
            function () {
                return (
                    app.state.currentRoomId
                );
            },

        getChatMode:
            function () {
                return (
                    app.state.chatMode
                );
            },

        getLoginUser:
            function () {
                return {
                    userNum:
                    app.state.loginUser
                        .userNum,

                    userId:
                    app.state.loginUser
                        .userId,

                    name:
                    app.state.loginUser
                        .name
                };
            }
    };


    /* =====================================================
       기존 HTML onclick 함수 호환
    ===================================================== */

    window.openChat =
        openChat;

    window.closeChat =
        closeChat;

    window.switchTab =
        switchTab;

    window.sendMessage =
        sendMessage;

    window.selectUser =
        selectUser;


    /* =====================================================
       페이지 로드 후 채팅 초기화
    ===================================================== */

    if (
        document.readyState ===
        "loading"
    ) {
        document.addEventListener(
            "DOMContentLoaded",
            initializeChat
        );

    } else {
        initializeChat();
    }

})(
    window.WorkTopusChat =
        window.WorkTopusChat || {}
);