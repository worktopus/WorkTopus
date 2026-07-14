(function (app) {
    "use strict";


    /* =====================================================
       채팅 내역 API
    ===================================================== */

    const HISTORY_API =
        "/chat/history";


    /* =====================================================
       조회 요청 상태

       채팅방을 빠르게 전환했을 때 이전 방의 응답이
       현재 화면에 출력되는 것을 방지합니다.
    ===================================================== */

    let activeRequestController =
        null;

    let latestRequestNumber =
        0;


    /* =====================================================
       이전 채팅 내역 조회
    ===================================================== */

    async function loadHistory(roomId) {
        const normalizedRoomId =
            normalizeRoomId(roomId);

        if (!normalizedRoomId) {
            console.warn(
                "채팅 내역을 조회할 roomId가 없습니다."
            );

            return [];
        }

        cancelActiveRequest();

        activeRequestController =
            new AbortController();

        const requestNumber =
            ++latestRequestNumber;

        showHistoryLoading(
            normalizedRoomId
        );

        try {
            /*
            로그인한 사용자는 서버 세션 또는
            Spring Security에서 직접 확인합니다.

            userNum을 URL 파라미터로 보내지 않습니다.
            */
            const response =
                await fetch(
                    `${HISTORY_API}/${encodeURIComponent(normalizedRoomId)}`,
                    {
                        method: "GET",

                        headers: {
                            "Accept":
                                "application/json"
                        },

                        signal:
                        activeRequestController.signal
                    }
                );

            if (!response.ok) {
                throw new Error(
                    `채팅 내역 조회 실패: ${response.status}`
                );
            }

            const data =
                await response.json();

            /*
            이 요청 이후 새로운 채팅방 조회가 시작됐다면
            이전 응답은 사용하지 않습니다.
            */
            if (
                requestNumber !==
                latestRequestNumber
            ) {
                return [];
            }

            /*
            조회가 완료되기 전에 다른 채팅방으로 이동했다면
            현재 화면에 이전 방의 메시지를 출력하지 않습니다.
            */
            if (
                normalizedRoomId !==
                getCurrentRoomId()
            ) {
                return [];
            }

            const messages =
                normalizeHistoryResponse(
                    data,
                    normalizedRoomId
                );

            renderHistory(
                normalizedRoomId,
                messages
            );

            return messages;

        } catch (error) {
            if (
                error.name ===
                "AbortError"
            ) {
                return [];
            }

            console.error(
                "채팅 내역 조회 오류",
                error
            );

            if (
                requestNumber ===
                latestRequestNumber &&
                normalizedRoomId ===
                getCurrentRoomId()
            ) {
                showHistoryError(
                    normalizedRoomId
                );
            }

            return [];

        } finally {
            if (
                requestNumber ===
                latestRequestNumber
            ) {
                activeRequestController =
                    null;
            }
        }
    }


    /* =====================================================
       서버 응답에서 메시지 목록 추출
    ===================================================== */

    function normalizeHistoryResponse(
        data,
        roomId
    ) {
        let messageList = [];

        if (Array.isArray(data)) {
            messageList =
                data;

        } else if (
            data &&
            Array.isArray(data.messages)
        ) {
            messageList =
                data.messages;

        } else if (
            data &&
            Array.isArray(data.content)
        ) {
            messageList =
                data.content;

        } else if (
            data &&
            Array.isArray(data.chatMessages)
        ) {
            messageList =
                data.chatMessages;
        }

        return messageList
            .map(
                function (message) {
                    return normalizeMessage(
                        message,
                        roomId
                    );
                }
            )
            .sort(compareMessages);
    }


    /* =====================================================
       메시지 데이터 정리

       senderNum  : 사용자 숫자 PK
       senderName : 화면에 표시할 사용자 이름
    ===================================================== */

    function normalizeMessage(
        message,
        defaultRoomId
    ) {
        const messageId =
            message.messageId ??
            message.chatMessageId ??
            message.id ??
            null;

        const senderNum =
            normalizeNumber(
                message.senderNum ??
                message.senderUserNum ??
                message.userNum ??
                message.senderId ??
                message.memberId
            );

        const projectId =
            normalizeNumber(
                message.projectId
            );

        return {
            messageId:
                messageId !== null &&
                messageId !== undefined &&
                messageId !== ""
                    ? String(messageId)
                    : null,

            projectId:
            projectId,

            roomId:
                normalizeRoomId(
                    message.roomId ??
                    defaultRoomId
                ),

            senderNum:
            senderNum,

            senderName:
                String(
                    message.senderName ??
                    message.userName ??
                    message.sender ??
                    message.memberName ??
                    ""
                ),

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
                message.time ??
                null,

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
       메시지를 작성 시간순으로 정렬
    ===================================================== */

    function compareMessages(
        firstMessage,
        secondMessage
    ) {
        const firstTime =
            getMessageTimestamp(
                firstMessage
            );

        const secondTime =
            getMessageTimestamp(
                secondMessage
            );

        if (
            firstTime !== secondTime
        ) {
            return (
                firstTime -
                secondTime
            );
        }

        /*
        작성 시간이 동일하면 messageId 순서로 정렬합니다.
        */
        const firstId =
            normalizeNumber(
                firstMessage.messageId,
                0
            );

        const secondId =
            normalizeNumber(
                secondMessage.messageId,
                0
            );

        return (
            firstId -
            secondId
        );
    }


    /* =====================================================
       메시지 시간 숫자 변환
    ===================================================== */

    function getMessageTimestamp(
        message
    ) {
        if (
            !message ||
            !message.createdAt
        ) {
            return 0;
        }

        const timestamp =
            new Date(
                message.createdAt
            ).getTime();

        return Number.isNaN(
            timestamp
        )
            ? 0
            : timestamp;
    }


    /* =====================================================
       채팅 내역 화면 출력
    ===================================================== */

    function renderHistory(
        roomId,
        messages
    ) {
        if (isGroupRoom(roomId)) {
            renderGroupHistory(
                messages
            );

            return;
        }

        renderPrivateHistory(
            messages
        );
    }


    /* =====================================================
       단체 채팅 내역 출력
    ===================================================== */

    function renderGroupHistory(
        messages
    ) {
        if (
            app.groupChat &&
            typeof app.groupChat
                .renderMessages ===
            "function"
        ) {
            app.groupChat.renderMessages(
                messages
            );

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

        if (
            typeof window.showGroupMessage !==
            "function"
        ) {
            return;
        }

        messages.forEach(
            function (message) {
                window.showGroupMessage(
                    message,
                    false
                );
            }
        );
    }


    /* =====================================================
       개인 채팅 내역 출력
    ===================================================== */

    function renderPrivateHistory(
        messages
    ) {
        if (
            app.privateChat &&
            typeof app.privateChat
                .renderMessages ===
            "function"
        ) {
            app.privateChat.renderMessages(
                messages
            );

            return;
        }

        const messageArea =
            getPrivateMessageArea();

        if (messageArea) {
            messageArea.innerHTML =
                "";
        }

        if (
            typeof window.showPrivateMessage !==
            "function"
        ) {
            return;
        }

        messages.forEach(
            function (message) {
                window.showPrivateMessage(
                    message,
                    false
                );
            }
        );
    }


    /* =====================================================
       채팅 내역 로딩 표시
    ===================================================== */

    function showHistoryLoading(
        roomId
    ) {
        const messageArea =
            getMessageArea(
                roomId
            );

        if (!messageArea) {
            return;
        }

        messageArea.innerHTML = `
            <div class="chat-history-loading">
                이전 대화를 불러오는 중입니다.
            </div>
        `;
    }


    /* =====================================================
       채팅 내역 조회 오류 표시
    ===================================================== */

    function showHistoryError(
        roomId
    ) {
        const messageArea =
            getMessageArea(
                roomId
            );

        if (!messageArea) {
            return;
        }

        messageArea.innerHTML = `
            <div class="chat-history-error">
                <p>
                    이전 대화를 불러오지 못했습니다.
                </p>

                <button
                    type="button"
                    class="chat-history-reload"
                >
                    다시 불러오기
                </button>
            </div>
        `;

        const reloadButton =
            messageArea.querySelector(
                ".chat-history-reload"
            );

        if (reloadButton) {
            reloadButton.addEventListener(
                "click",
                function () {
                    loadHistory(
                        roomId
                    );
                }
            );
        }
    }


    /* =====================================================
       현재 채팅 종류에 맞는 메시지 영역 반환
    ===================================================== */

    function getMessageArea(
        roomId
    ) {
        if (isGroupRoom(roomId)) {
            return document.getElementById(
                "messageArea"
            );
        }

        return getPrivateMessageArea();
    }


    /* =====================================================
       개인 채팅 메시지 영역 반환
    ===================================================== */

    function getPrivateMessageArea() {
        let messageArea =
            document.getElementById(
                "dmArea"
            );

        if (messageArea) {
            return messageArea;
        }

        const privateMessageArea =
            document.getElementById(
                "privateMessageArea"
            );

        if (!privateMessageArea) {
            return null;
        }

        messageArea =
            document.createElement(
                "div"
            );

        messageArea.id =
            "dmArea";

        privateMessageArea.appendChild(
            messageArea
        );

        return messageArea;
    }


    /* =====================================================
       현재 선택된 채팅방 ID
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
       진행 중인 조회 요청 취소
    ===================================================== */

    function cancelActiveRequest() {
        if (
            !activeRequestController
        ) {
            return;
        }

        activeRequestController.abort();

        activeRequestController =
            null;
    }


    /* =====================================================
       단체 채팅방 여부
    ===================================================== */

    function isGroupRoom(
        roomId
    ) {
        return normalizeRoomId(
            roomId
        ).endsWith(
            "_group"
        );
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
        value,
        defaultValue = null
    ) {
        if (
            value === null ||
            value === undefined ||
            value === ""
        ) {
            return defaultValue;
        }

        const number =
            Number(value);

        return Number.isFinite(number)
            ? number
            : defaultValue;
    }


    /* =====================================================
       외부 파일에서 사용할 함수
    ===================================================== */

    app.history = {
        loadHistory,
        cancelActiveRequest
    };


    /* =====================================================
       기존 함수 호출 호환
    ===================================================== */

    window.loadHistory =
        loadHistory;

})(
    window.WorkTopusChat =
        window.WorkTopusChat || {}
);