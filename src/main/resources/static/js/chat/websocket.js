(function (app) {
    "use strict";


    /* =====================================================
       WebSocket 주소 설정
    ===================================================== */

    const SOCKET_ENDPOINT =
        "/chat";

    const CHAT_SEND_DESTINATION =
        "/app/chat.send";

    const CHAT_SUBSCRIBE_PREFIX =
        "/topic/chat/";

    const PRESENCE_DESTINATION =
        "/topic/presence";

    const RECONNECT_DELAY =
        3000;


    /* =====================================================
       WebSocket 상태
    ===================================================== */

    let stompClient =
        null;

    let roomSubscription =
        null;

    let presenceSubscription =
        null;

    let subscribedRoomId =
        null;

    let pendingRoomId =
        null;

    let reconnectTimer =
        null;

    let connecting =
        false;

    let manuallyDisconnected =
        false;


    /* =====================================================
       WebSocket 연결
    ===================================================== */

    function connect() {
        if (
            isConnected() ||
            connecting
        ) {
            return;
        }

        if (
            typeof window.SockJS ===
            "undefined"
        ) {
            console.error(
                "SockJS 라이브러리가 없습니다."
            );

            notifyConnectionChange(false);

            return;
        }

        if (
            typeof window.Stomp ===
            "undefined"
        ) {
            console.error(
                "STOMP 라이브러리가 없습니다."
            );

            notifyConnectionChange(false);

            return;
        }

        manuallyDisconnected =
            false;

        connecting =
            true;

        clearReconnectTimer();

        const socket =
            new SockJS(
                SOCKET_ENDPOINT
            );

        stompClient =
            Stomp.over(socket);

        /*
        STOMP 디버그 로그를 끕니다.
        필요할 때는 아래 줄을 주석 처리합니다.
        */
        stompClient.debug =
            null;

        syncLegacyClient();

        /*
        로그인 정보는 기존 세션 또는
        Spring Security 인증에서 서버가 확인합니다.

        userNum을 연결 헤더로 보내서
        인증값으로 사용하지 않습니다.
        */
        stompClient.connect(
            {},
            handleConnectSuccess,
            handleConnectError
        );
    }


    /* =====================================================
       WebSocket 연결 성공
    ===================================================== */

    function handleConnectSuccess(
        frame
    ) {
        connecting =
            false;

        console.log(
            "WebSocket 연결 성공",
            frame
        );

        notifyConnectionChange(
            true
        );

        subscribePresence();

        const roomId =
            pendingRoomId ||
            getCurrentRoomId();

        if (roomId) {
            subscribeRoom(
                roomId
            );
        }
    }


    /* =====================================================
       WebSocket 연결 실패
    ===================================================== */

    function handleConnectError(
        error
    ) {
        connecting =
            false;

        console.error(
            "WebSocket 연결 오류",
            error
        );

        cleanupConnection();

        notifyConnectionChange(
            false
        );

        if (
            !manuallyDisconnected
        ) {
            scheduleReconnect();
        }
    }


    /* =====================================================
       WebSocket 재연결
    ===================================================== */

    function scheduleReconnect() {
        if (
            reconnectTimer ||
            manuallyDisconnected
        ) {
            return;
        }

        reconnectTimer =
            window.setTimeout(
                function () {
                    reconnectTimer =
                        null;

                    connect();
                },
                RECONNECT_DELAY
            );
    }


    function clearReconnectTimer() {
        if (!reconnectTimer) {
            return;
        }

        window.clearTimeout(
            reconnectTimer
        );

        reconnectTimer =
            null;
    }


    /* =====================================================
       채팅방 구독
    ===================================================== */

    function subscribeRoom(roomId) {
        const normalizedRoomId =
            normalizeRoomId(
                roomId
            );

        if (!normalizedRoomId) {
            console.warn(
                "구독할 채팅방 ID가 없습니다."
            );

            return false;
        }

        /*
        연결 전이라도 선택한 방을 저장합니다.
        연결 성공 후 해당 방을 자동 구독합니다.
        */
        pendingRoomId =
            normalizedRoomId;

        if (!isConnected()) {
            connect();

            return false;
        }

        /*
        이미 같은 방을 구독 중이면
        다시 구독하지 않습니다.
        */
        if (
            roomSubscription &&
            subscribedRoomId ===
            normalizedRoomId
        ) {
            return true;
        }

        unsubscribeCurrentRoom();

        const destination =
            CHAT_SUBSCRIBE_PREFIX +
            normalizedRoomId;

        try {
            roomSubscription =
                stompClient.subscribe(
                    destination,
                    handleChatMessage
                );

            subscribedRoomId =
                normalizedRoomId;

            pendingRoomId =
                normalizedRoomId;

            console.log(
                "채팅방 구독 완료:",
                destination
            );

            return true;

        } catch (error) {
            console.error(
                "채팅방 구독 실패",
                error
            );

            roomSubscription =
                null;

            subscribedRoomId =
                null;

            return false;
        }
    }


    /* =====================================================
       현재 채팅방 구독 해제
    ===================================================== */

    function unsubscribeCurrentRoom() {
        if (roomSubscription) {
            try {
                roomSubscription
                    .unsubscribe();

            } catch (error) {
                console.warn(
                    "채팅방 구독 해제 오류",
                    error
                );
            }
        }

        roomSubscription =
            null;

        subscribedRoomId =
            null;
    }


    /* =====================================================
       실시간 접속 상태 구독
    ===================================================== */

    function subscribePresence() {
        if (
            !isConnected() ||
            presenceSubscription
        ) {
            return;
        }

        try {
            presenceSubscription =
                stompClient.subscribe(
                    PRESENCE_DESTINATION,
                    handlePresenceMessage
                );

        } catch (error) {
            console.warn(
                "접속 상태 구독 실패",
                error
            );

            presenceSubscription =
                null;
        }
    }


    /* =====================================================
       접속 상태 메시지 수신
    ===================================================== */

    function handlePresenceMessage(
        stompMessage
    ) {
        const rawStatus =
            parseStompBody(
                stompMessage
            );

        if (!rawStatus) {
            return;
        }

        const status =
            normalizePresenceStatus(
                rawStatus
            );

        if (
            status.userNum === null
        ) {
            return;
        }

        if (
            app.project &&
            typeof app.project
                .updateMemberOnlineStatus ===
            "function"
        ) {
            app.project
                .updateMemberOnlineStatus(
                    status
                );
        }

        if (
            app.member &&
            typeof app.member
                .updateOnlineStatus ===
            "function"
        ) {
            app.member
                .updateOnlineStatus(
                    status
                );
        }
    }


    /* =====================================================
       접속 상태 데이터 정리
    ===================================================== */

    function normalizePresenceStatus(
        status
    ) {
        return {
            projectId:
                normalizeNumber(
                    status.projectId
                ),

            userNum:
                normalizeNumber(
                    status.userNum ??
                    status.memberId ??
                    status.id
                ),

            name:
                String(
                    status.name ??
                    status.userName ??
                    status.memberName ??
                    ""
                ),

            online:
                normalizeBoolean(
                    status.online ??
                    status.isOnline ??
                    status.onlineYn
                )
        };
    }


    /* =====================================================
       채팅 메시지 수신
    ===================================================== */

    function handleChatMessage(
        stompMessage
    ) {
        const rawMessage =
            parseStompBody(
                stompMessage
            );

        if (!rawMessage) {
            return;
        }

        const message =
            normalizeReceivedMessage(
                rawMessage
            );

        if (
            app.chat &&
            typeof app.chat
                .handleIncomingMessage ===
            "function"
        ) {
            app.chat
                .handleIncomingMessage(
                    message
                );

            return;
        }

        handleLegacyIncomingMessage(
            message
        );
    }


    /* =====================================================
       STOMP 응답 JSON 변환
    ===================================================== */

    function parseStompBody(
        stompMessage
    ) {
        if (
            !stompMessage ||
            typeof stompMessage.body !==
            "string"
        ) {
            return null;
        }

        try {
            return JSON.parse(
                stompMessage.body
            );

        } catch (error) {
            console.error(
                "WebSocket 메시지 변환 실패",
                error,
                stompMessage.body
            );

            return null;
        }
    }


    /* =====================================================
       수신 메시지 데이터 정리

       senderNum은 사용자 숫자 PK
       senderName은 화면 표시 이름
    ===================================================== */

    function normalizeReceivedMessage(
        message
    ) {
        const senderNum =
            normalizeNumber(
                message.senderNum ??
                message.senderUserNum ??
                message.userNum ??
                message.senderId ??
                message.memberId
            );

        const senderName =
            String(
                message.senderName ??
                message.userName ??
                message.sender ??
                message.memberName ??
                ""
            );

        return {
            ...message,

            messageId:
                message.messageId ??
                message.chatMessageId ??
                message.id ??
                null,

            projectId:
                normalizeNumber(
                    message.projectId
                ),

            roomId:
                normalizeRoomId(
                    message.roomId
                ),

            senderNum:
            senderNum,

            senderName:
            senderName,

            message:
                String(
                    message.message ??
                    message.content ??
                    ""
                ),

            type:
                String(
                    message.type ??
                    message.messageType ??
                    "TALK"
                ).toUpperCase(),

            createdAt:
                message.createdAt ??
                message.sentAt ??
                message.regDate ??
                new Date()
                    .toISOString(),

            unreadCount:
                message.unreadCount ??
                message.notReadCount ??
                null,

            readYn:
                message.readYn ??
                message.isRead ??
                null
        };
    }


    /* =====================================================
       채팅 메시지 전송
    ===================================================== */

    function send(message) {
        if (!message) {
            return false;
        }

        if (!isConnected()) {
            console.warn(
                "WebSocket 연결 전에는 메시지를 보낼 수 없습니다."
            );

            connect();

            return false;
        }

        const roomId =
            normalizeRoomId(
                message.roomId
            );

        if (!roomId) {
            console.warn(
                "메시지에 roomId가 없습니다."
            );

            return false;
        }

        const projectId =
            normalizeNumber(
                message.projectId
            );

        const loginUser =
            getLoginUser();

        /*
        senderNum과 senderName은 현재 개발 단계의
        화면 출력 및 기존 서버 호환을 위해 포함합니다.

        최종 백엔드에서는 프런트가 보낸 값을 믿지 않고
        로그인 세션의 사용자 정보로 다시 설정해야 합니다.
        */
        const payload = {
            projectId:
            projectId,

            roomId:
            roomId,

            senderNum:
            loginUser.userNum,

            senderName:
            loginUser.name,

            message:
                String(
                    message.message ??
                    message.content ??
                    ""
                ),

            type:
                String(
                    message.type ??
                    "TALK"
                ).toUpperCase(),

            createdAt:
                message.createdAt ??
                new Date()
                    .toISOString()
        };

        if (!payload.message.trim()) {
            return false;
        }

        try {
            stompClient.send(
                CHAT_SEND_DESTINATION,
                {},
                JSON.stringify(
                    payload
                )
            );

            return true;

        } catch (error) {
            console.error(
                "WebSocket 메시지 전송 실패",
                error
            );

            notifyConnectionChange(
                false
            );

            scheduleReconnect();

            return false;
        }
    }


    /* =====================================================
       WebSocket 연결 종료
    ===================================================== */

    function disconnect() {
        manuallyDisconnected =
            true;

        connecting =
            false;

        clearReconnectTimer();

        unsubscribeCurrentRoom();

        unsubscribePresence();

        pendingRoomId =
            null;

        if (
            !stompClient ||
            !stompClient.connected
        ) {
            stompClient =
                null;

            syncLegacyClient();

            notifyConnectionChange(
                false
            );

            return;
        }

        try {
            stompClient.disconnect(
                function () {
                    console.log(
                        "WebSocket 연결 종료"
                    );

                    stompClient =
                        null;

                    syncLegacyClient();

                    notifyConnectionChange(
                        false
                    );
                }
            );

        } catch (error) {
            console.warn(
                "WebSocket 연결 종료 오류",
                error
            );

            stompClient =
                null;

            syncLegacyClient();

            notifyConnectionChange(
                false
            );
        }
    }


    /* =====================================================
       접속 상태 구독 해제
    ===================================================== */

    function unsubscribePresence() {
        if (!presenceSubscription) {
            return;
        }

        try {
            presenceSubscription
                .unsubscribe();

        } catch (error) {
            console.warn(
                "접속 상태 구독 해제 오류",
                error
            );
        }

        presenceSubscription =
            null;
    }


    /* =====================================================
       연결 오류 상태 초기화
    ===================================================== */

    function cleanupConnection() {
        roomSubscription =
            null;

        presenceSubscription =
            null;

        subscribedRoomId =
            null;

        stompClient =
            null;

        syncLegacyClient();
    }


    /* =====================================================
       WebSocket 연결 여부
    ===================================================== */

    function isConnected() {
        return Boolean(
            stompClient &&
            stompClient.connected
        );
    }


    /* =====================================================
       현재 구독 중인 방
    ===================================================== */

    function getSubscribedRoomId() {
        return subscribedRoomId;
    }


    /* =====================================================
       현재 선택된 채팅방
    ===================================================== */

    function getCurrentRoomId() {
        if (
            app.chat &&
            typeof app.chat
                .getCurrentRoomId ===
            "function"
        ) {
            return normalizeRoomId(
                app.chat
                    .getCurrentRoomId()
            );
        }

        if (
            app.state &&
            app.state.currentRoomId
        ) {
            return normalizeRoomId(
                app.state.currentRoomId
            );
        }

        return normalizeRoomId(
            window.currentRoom
        );
    }


    /* =====================================================
       로그인 사용자 정보
    ===================================================== */

    function getLoginUser() {
        if (
            app.chat &&
            typeof app.chat
                .getLoginUser ===
            "function"
        ) {
            const loginUser =
                app.chat.getLoginUser();

            return {
                userNum:
                    normalizeNumber(
                        loginUser?.userNum
                    ),

                userId:
                    String(
                        loginUser?.userId ??
                        ""
                    ),

                name:
                    String(
                        loginUser?.name ??
                        ""
                    )
            };
        }

        if (
            app.state &&
            app.state.loginUser
        ) {
            return {
                userNum:
                    normalizeNumber(
                        app.state
                            .loginUser
                            .userNum
                    ),

                userId:
                    String(
                        app.state
                            .loginUser
                            .userId ??
                        ""
                    ),

                name:
                    String(
                        app.state
                            .loginUser
                            .name ??
                        ""
                    )
            };
        }

        return {
            userNum: null,
            userId: "",
            name: ""
        };
    }


    /* =====================================================
       연결 상태를 chat.js에 전달
    ===================================================== */

    function notifyConnectionChange(
        connected
    ) {
        if (app.state) {
            app.state.socketConnected =
                Boolean(connected);
        }

        if (
            app.chat &&
            typeof app.chat
                .handleConnectionChange ===
            "function"
        ) {
            app.chat
                .handleConnectionChange(
                    Boolean(
                        connected
                    )
                );
        }
    }


    /* =====================================================
       기존 메시지 출력 함수 호환
    ===================================================== */

    function handleLegacyIncomingMessage(
        message
    ) {
        const roomId =
            normalizeRoomId(
                message.roomId
            );

        if (
            roomId.endsWith(
                "_group"
            )
        ) {
            if (
                typeof window
                    .showGroupMessage ===
                "function"
            ) {
                window.showGroupMessage(
                    message
                );
            }

            return;
        }

        if (
            typeof window
                .showPrivateMessage ===
            "function"
        ) {
            window.showPrivateMessage(
                message
            );
        }
    }


    /* =====================================================
       채팅방 ID 정리
    ===================================================== */

    function normalizeRoomId(
        roomId
    ) {
        if (
            roomId === null ||
            roomId === undefined
        ) {
            return "";
        }

        return String(
            roomId
        ).trim();
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
       Boolean 변환
    ===================================================== */

    function normalizeBoolean(value) {
        if (
            value === true ||
            value === 1
        ) {
            return true;
        }

        if (
            value === false ||
            value === 0 ||
            value === null ||
            value === undefined
        ) {
            return false;
        }

        const normalizedValue =
            String(value)
                .trim()
                .toLowerCase();

        return (
            normalizedValue === "true" ||
            normalizedValue === "y" ||
            normalizedValue === "yes" ||
            normalizedValue === "1" ||
            normalizedValue === "online"
        );
    }


    /* =====================================================
       기존 전역 stompClient와 동기화
    ===================================================== */

    function syncLegacyClient() {
        window.stompClient =
            stompClient;
    }


    /* =====================================================
       외부 파일에서 사용할 함수
    ===================================================== */

    app.websocket = {
        connect,
        disconnect,

        subscribeRoom,
        unsubscribeCurrentRoom,

        send,
        isConnected,

        getSubscribedRoomId
    };


    /* =====================================================
       기존 함수 호출 호환
    ===================================================== */

    window.connect =
        connect;

    window.disconnect =
        disconnect;

    window.subscribeRoom =
        subscribeRoom;


    /* =====================================================
       페이지를 나갈 때 WebSocket 종료
    ===================================================== */

    window.addEventListener(
        "beforeunload",
        function () {
            disconnect();
        }
    );

})(
    window.WorkTopusChat =
        window.WorkTopusChat || {}
);