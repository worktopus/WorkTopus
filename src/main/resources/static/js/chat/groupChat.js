(function (app) {
    "use strict";


    /* =====================================================
       화면에 출력한 메시지 ID

       DB 내역과 WebSocket 메시지가 중복으로 들어와도
       같은 messageId는 한 번만 출력합니다.
    ===================================================== */

    const renderedMessageIds =
        new Set();


    /* =====================================================
       단체 채팅 메시지 한 개 출력
    ===================================================== */

    function showMessage(
        message,
        shouldScroll = true
    ) {
        if (!message) {
            return;
        }

        const messageArea =
            document.getElementById(
                "messageArea"
            );

        if (!messageArea) {
            return;
        }

        const messageId =
            getMessageId(message);

        if (
            messageId !== null &&
            renderedMessageIds.has(
                messageId
            )
        ) {
            return;
        }

        removeTemporaryMessages(
            messageArea
        );

        const messageType =
            getMessageType(message);

        const messageElement =
            isSystemMessage(messageType)
                ? createSystemMessageElement(
                    message,
                    messageType
                )
                : createChatMessageElement(
                    message
                );

        messageArea.appendChild(
            messageElement
        );

        if (messageId !== null) {
            renderedMessageIds.add(
                messageId
            );
        }

        if (shouldScroll) {
            scrollToBottom();
        }
    }


    /* =====================================================
       이전 단체 채팅 내역 전체 출력
    ===================================================== */

    function renderMessages(messages) {
        clearMessages();

        if (
            !Array.isArray(messages) ||
            messages.length === 0
        ) {
            showEmptyMessage();
            return;
        }

        messages.forEach(
            function (message) {
                showMessage(
                    message,
                    false
                );
            }
        );

        scrollToBottom(false);
    }


    /* =====================================================
       일반 메시지 HTML 생성
    ===================================================== */

    function createChatMessageElement(
        message
    ) {
        const mine =
            isMyMessage(message);

        const wrapper =
            document.createElement(
                "div"
            );

        /*
        기존 chat.css의 sent / received 디자인을
        그대로 사용합니다.
        */
        wrapper.className =
            mine
                ? "chat-message sent"
                : "chat-message received";

        const messageId =
            getMessageId(message);

        if (messageId !== null) {
            wrapper.dataset.messageId =
                messageId;
        }

        /*
        사용자 이름은 senderName으로 표시합니다.
        userNum은 화면에 출력하지 않습니다.
        */
        const senderElement =
            document.createElement(
                "strong"
            );

        senderElement.textContent =
            getSenderName(message) ||
            "알 수 없는 사용자";

        wrapper.appendChild(
            senderElement
        );

        const contentElement =
            document.createElement(
                "div"
            );

        contentElement.textContent =
            getMessageContent(
                message
            );

        wrapper.appendChild(
            contentElement
        );

        const timeElement =
            document.createElement(
                "span"
            );

        timeElement.className =
            "chat-message__time";

        timeElement.textContent =
            formatMessageTime(
                getCreatedAt(message)
            );

        wrapper.appendChild(
            timeElement
        );

        return wrapper;
    }


    /* =====================================================
       입장 / 퇴장 / 안내 메시지 생성
    ===================================================== */

    function createSystemMessageElement(
        message,
        messageType
    ) {
        const wrapper =
            document.createElement(
                "div"
            );

        /*
        별도 CSS를 추가하지 않고 기존 받은 메시지
        디자인을 사용합니다.
        */
        wrapper.className =
            "chat-message received";

        const contentElement =
            document.createElement(
                "div"
            );

        const content =
            getMessageContent(
                message
            );

        const senderName =
            getSenderName(
                message
            ) || "사용자";

        if (content) {
            contentElement.textContent =
                content;

        } else if (
            messageType === "ENTER"
        ) {
            contentElement.textContent =
                `${senderName}님이 입장했습니다.`;

        } else if (
            messageType === "LEAVE"
        ) {
            contentElement.textContent =
                `${senderName}님이 퇴장했습니다.`;

        } else {
            contentElement.textContent =
                "채팅 안내 메시지입니다.";
        }

        wrapper.appendChild(
            contentElement
        );

        const timeElement =
            document.createElement(
                "span"
            );

        timeElement.className =
            "chat-message__time";

        timeElement.textContent =
            formatMessageTime(
                getCreatedAt(message)
            );

        wrapper.appendChild(
            timeElement
        );

        return wrapper;
    }


    /* =====================================================
       내 메시지 여부 확인

       senderNum과 로그인 사용자의 userNum을 비교합니다.
       이름은 사용자 식별 기준으로 사용하지 않는 것이 원칙입니다.
    ===================================================== */

    function isMyMessage(message) {
        const loginUser =
            getLoginUser();

        const senderNum =
            getSenderNum(
                message
            );

        if (
            senderNum !== null &&
            loginUser.userNum !== null
        ) {
            return (
                senderNum ===
                loginUser.userNum
            );
        }

        /*
        이전 서버 응답처럼 senderNum이 없는 경우를 위한
        임시 호환 처리입니다.
        */
        const senderName =
            getSenderName(
                message
            );

        return Boolean(
            senderName &&
            loginUser.name &&
            senderName ===
            loginUser.name
        );
    }


    /* =====================================================
       로그인 사용자 조회
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
            userNum:
                normalizeNumber(
                    window.currentUserNum ??
                    window.currentUserId
                ),

            name:
                String(
                    window.currentUser ??
                    ""
                )
        };
    }


    /* =====================================================
       메시지 ID 조회
    ===================================================== */

    function getMessageId(message) {
        const messageId =
            message.messageId ??
            message.chatMessageId ??
            message.id ??
            null;

        if (
            messageId === null ||
            messageId === undefined ||
            messageId === ""
        ) {
            return null;
        }

        return String(
            messageId
        );
    }


    /* =====================================================
       메시지 발신자 userNum 조회
    ===================================================== */

    function getSenderNum(message) {
        return normalizeNumber(
            message.senderNum ??
            message.senderUserNum ??
            message.userNum ??
            message.senderId ??
            message.memberId ??
            null
        );
    }


    /* =====================================================
       메시지 발신자 이름 조회
    ===================================================== */

    function getSenderName(message) {
        return String(
            message.senderName ??
            message.userName ??
            message.sender ??
            message.memberName ??
            ""
        );
    }


    /* =====================================================
       메시지 내용 조회
    ===================================================== */

    function getMessageContent(message) {
        return String(
            message.message ??
            message.content ??
            ""
        );
    }


    /* =====================================================
       메시지 작성 시간 조회
    ===================================================== */

    function getCreatedAt(message) {
        return (
            message.createdAt ??
            message.sentAt ??
            message.regDate ??
            message.time ??
            null
        );
    }


    /* =====================================================
       메시지 종류 조회
    ===================================================== */

    function getMessageType(message) {
        return String(
            message.type ??
            message.messageType ??
            "TALK"
        ).toUpperCase();
    }


    function isSystemMessage(
        messageType
    ) {
        return (
            messageType === "ENTER" ||
            messageType === "LEAVE" ||
            messageType === "SYSTEM" ||
            messageType === "NOTICE"
        );
    }


    /* =====================================================
       단체 채팅 화면 초기화
    ===================================================== */

    function clearMessages() {
        const messageArea =
            document.getElementById(
                "messageArea"
            );

        if (!messageArea) {
            return;
        }

        messageArea.innerHTML =
            "";

        renderedMessageIds.clear();
    }


    /* =====================================================
       대화가 없는 화면
    ===================================================== */

    function showEmptyMessage() {
        const messageArea =
            document.getElementById(
                "messageArea"
            );

        if (!messageArea) {
            return;
        }

        messageArea.innerHTML =
            "";

        const emptyElement =
            document.createElement(
                "div"
            );

        emptyElement.className =
            "chat-empty";

        const iconElement =
            document.createElement(
                "div"
            );

        iconElement.className =
            "chat-empty__icon";

        iconElement.textContent =
            "💬";

        const titleElement =
            document.createElement(
                "strong"
            );

        titleElement.textContent =
            "아직 대화가 없습니다.";

        const descriptionElement =
            document.createElement(
                "span"
            );

        descriptionElement.textContent =
            "프로젝트 팀원들과 첫 메시지를 나눠보세요.";

        emptyElement.appendChild(
            iconElement
        );

        emptyElement.appendChild(
            titleElement
        );

        emptyElement.appendChild(
            descriptionElement
        );

        messageArea.appendChild(
            emptyElement
        );
    }


    /* =====================================================
       로딩 / 오류 / 빈 화면 제거
    ===================================================== */

    function removeTemporaryMessages(
        messageArea
    ) {
        const selectors = [
            ".chat-empty",
            ".chat-empty-message",
            ".chat-history-loading",
            ".chat-history-error"
        ];

        selectors.forEach(
            function (selector) {
                messageArea
                    .querySelectorAll(
                        selector
                    )
                    .forEach(
                        function (element) {
                            element.remove();
                        }
                    );
            }
        );
    }


    /* =====================================================
       메시지 시간 표시
    ===================================================== */

    function formatMessageTime(
        createdAt
    ) {
        if (!createdAt) {
            return "";
        }

        const date =
            new Date(
                createdAt
            );

        if (
            Number.isNaN(
                date.getTime()
            )
        ) {
            return String(
                createdAt
            );
        }

        const today =
            new Date();

        const isToday =
            date.getFullYear() ===
            today.getFullYear() &&
            date.getMonth() ===
            today.getMonth() &&
            date.getDate() ===
            today.getDate();

        if (isToday) {
            return date.toLocaleTimeString(
                "ko-KR",
                {
                    hour: "numeric",
                    minute: "2-digit"
                }
            );
        }

        return date.toLocaleDateString(
            "ko-KR",
            {
                month: "numeric",
                day: "numeric",
                hour: "numeric",
                minute: "2-digit"
            }
        );
    }


    /* =====================================================
       숫자 변환
    ===================================================== */

    function normalizeNumber(value) {
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
       메시지 영역 맨 아래로 이동
    ===================================================== */

    function scrollToBottom(
        smooth = true
    ) {
        const messageArea =
            document.getElementById(
                "messageArea"
            );

        if (!messageArea) {
            return;
        }

        if (
            typeof messageArea.scrollTo ===
            "function"
        ) {
            messageArea.scrollTo({
                top:
                messageArea.scrollHeight,

                behavior:
                    smooth
                        ? "smooth"
                        : "auto"
            });

            return;
        }

        messageArea.scrollTop =
            messageArea.scrollHeight;
    }


    /* =====================================================
       외부 파일에서 사용할 함수
    ===================================================== */

    app.groupChat = {
        showMessage,
        renderMessages,
        clearMessages,
        showEmptyMessage,
        scrollToBottom
    };


    /* =====================================================
       기존 함수 호출 호환
    ===================================================== */

    window.showGroupMessage =
        showMessage;

})(
    window.WorkTopusChat =
        window.WorkTopusChat || {}
);