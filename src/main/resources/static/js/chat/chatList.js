(function (app) {
    "use strict";


    /* =====================================================
       상태
    ===================================================== */

    app.state = app.state || {};

    if (app.state.chatView === undefined) {
        app.state.chatView = "list";
    }

    if (app.state.roomListFilter === undefined) {
        app.state.roomListFilter = "all";
    }

    const ROOM_SUMMARY_API =
        "/api/chat/rooms";

    let chatRooms = [];

    /*
     * WebSocket으로 갱신된 마지막 메시지와
     * 안 읽은 개수를 임시로 보관합니다.
     */
    const roomStateCache = new Map();


    /* =====================================================
       초기화
    ===================================================== */

    function initializeChatList() {
        bindRoomListEvents();
        refreshRooms();
    }

    /* =====================================================
       채팅방 목록 생성
    ===================================================== */
    async function refreshRooms() {
        renderRoomListLoading();

        try {
            const response =
                await fetch(
                    ROOM_SUMMARY_API,
                    {
                        method: "GET",

                        headers: {
                            "Accept":
                                "application/json"
                        }
                    }
                );

            if (!response.ok) {
                throw new Error(
                    `채팅방 목록 조회 실패: ${response.status}`
                );
            }

            const data =
                await response.json();

            const summaries =
                Array.isArray(data)
                    ? data
                    : [];

            chatRooms =
                summaries
                    .map(
                        normalizeRoomSummary
                    )
                    .filter(Boolean)
                    .sort(
                        compareChatRooms
                    );

            renderChatRooms();

            return getRooms();

        } catch (error) {
            console.error(
                "채팅방 목록 API 조회 오류",
                error
            );

            /*
             * API 조회에 실패하면 기존 프로젝트 정보를 이용해
             * 최소한의 채팅방 목록을 표시합니다.
             */
            chatRooms =
                buildRoomsFromProjects();

            renderChatRooms();

            return getRooms();
        }
    }

    function normalizeRoomSummary(
        summary
    ) {
        if (!summary) {
            return null;
        }

        const roomId =
            String(
                summary.roomId ?? ""
            );

        const projectId =
            normalizeNumber(
                summary.projectId
            );

        const roomType =
            String(
                summary.roomType ?? ""
            ).toUpperCase();

        if (
            !roomId ||
            projectId === null ||
            !["GROUP", "PRIVATE"]
                .includes(roomType)
        ) {
            return null;
        }

        const projects =
            getProjects();

        let project =
            projects.find(
                function (item) {
                    return (
                        normalizeNumber(
                            item.id ??
                            item.projectId
                        ) === projectId
                    );
                }
            ) ?? null;

        const summaryMembers =
            Array.isArray(
                summary.members
            )
                ? summary.members
                : [];

        /*
         * 프로젝트 API에서 프로젝트를 찾지 못해도
         * 채팅방을 열 수 있도록 기본 프로젝트 객체를 생성합니다.
         */
        if (!project) {
            project = {
                id:
                projectId,

                projectId:
                projectId,

                name:
                    roomType === "GROUP"
                        ? String(
                            summary.roomName ??
                            "프로젝트"
                        )
                        : "프로젝트",

                projectName:
                    roomType === "GROUP"
                        ? String(
                            summary.roomName ??
                            "프로젝트"
                        )
                        : "프로젝트",

                roomId:
                    roomType === "GROUP"
                        ? roomId
                        : `project_${projectId}_group`,

                groupRoomId:
                    roomType === "GROUP"
                        ? roomId
                        : `project_${projectId}_group`,

                members:
                summaryMembers
            };
        }

        const members =
            summaryMembers.length > 0
                ? summaryMembers
                : (
                    Array.isArray(
                        project.members
                    )
                        ? project.members
                        : []
                );

        const targetMember =
            summary.targetMember ??
            findTargetMember(
                project,
                roomId
            );

        const serverLastMessage =
            normalizeLastMessage(
                summary.lastMessage
            );

        const cachedState =
            roomStateCache.get(
                roomId
            );

        const cachedLastMessage =
            normalizeLastMessage(
                cachedState?.lastMessage
            );

        /*
         * WebSocket으로 받은 메시지와 서버 메시지 중
         * 더 최신 메시지를 목록에 사용합니다.
         */
        const lastMessage =
            getNewerMessage(
                serverLastMessage,
                cachedLastMessage
            );

        const unreadCount =
            cachedState
                ? Math.max(
                    0,
                    Number(
                        cachedState.unreadCount ??
                        summary.unreadCount ??
                        0
                    )
                )
                : Math.max(
                    0,
                    Number(
                        summary.unreadCount ??
                        0
                    )
                );

        return {
            roomId:
            roomId,

            roomType:
            roomType,

            projectId:
            projectId,

            roomName:
                String(
                    summary.roomName ??
                    targetMember?.name ??
                    project.name ??
                    "채팅방"
                ),

            project:
            project,

            members:
            members,

            memberCount:
                Number(
                    summary.memberCount ??
                    (
                        roomType === "GROUP"
                            ? members.length
                            : 2
                    )
                ),

            targetMember:
                roomType === "PRIVATE"
                    ? targetMember
                    : null,

            online:
                Boolean(
                    summary.online ??
                    targetMember?.online
                ),

            lastMessage:
            lastMessage,

            unreadCount:
            unreadCount,

            updatedAt:
                lastMessage?.createdAt ??
                summary.updatedAt ??
                null
        };
    }

    function normalizeLastMessage(
        message
    ) {
        if (!message) {
            return null;
        }

        const messageText =
            String(
                message.message ?? ""
            );

        if (!messageText) {
            return null;
        }

        return {
            messageId:
                normalizeNumber(
                    message.messageId
                ),

            projectId:
                normalizeNumber(
                    message.projectId
                ),

            roomId:
                String(
                    message.roomId ?? ""
                ),

            senderNum:
                normalizeNumber(
                    message.senderNum
                ),

            senderName:
                String(
                    message.senderName ??
                    ""
                ),

            message:
            messageText,

            type:
                String(
                    message.type ??
                    "TALK"
                ),

            createdAt:
                message.createdAt ??
                null
        };
    }

    function buildRoomsFromProjects() {
        const projects =
            getProjects();

        const loginUserNum =
            getLoginUserNum();

        const nextRooms = [];

        projects.forEach(
            function (project) {

                /*
                 * 프로젝트 단체 채팅방
                 */
                const groupRoomId =
                    String(
                        project.roomId ??
                        project.groupRoomId ??
                        `project_${project.id}_group`
                    );

                const cachedGroupRoom =
                    roomStateCache.get(
                        groupRoomId
                    );

                nextRooms.push({
                    roomId:
                    groupRoomId,

                    roomType:
                        "GROUP",

                    projectId:
                    project.id,

                    roomName:
                    project.name,

                    project:
                    project,

                    members:
                        Array.isArray(
                            project.members
                        )
                            ? project.members
                            : [],

                    memberCount:
                        Array.isArray(
                            project.members
                        )
                            ? project.members.length
                            : 0,

                    lastMessage:
                        cachedGroupRoom
                            ?.lastMessage ??
                        project.lastMessage ??
                        null,

                    unreadCount:
                        cachedGroupRoom
                            ?.unreadCount ??
                        Number(
                            project.unreadCount ??
                            0
                        ),

                    updatedAt:
                        cachedGroupRoom
                            ?.lastMessage
                            ?.createdAt ??
                        project.lastMessage
                            ?.createdAt ??
                        null
                });


                /*
                 * 프로젝트 참여자별 개인 채팅방
                 */
                const members =
                    Array.isArray(
                        project.members
                    )
                        ? project.members
                        : [];

                members
                    .filter(
                        function (member) {
                            return (
                                Number(
                                    member.userNum
                                ) !==
                                loginUserNum
                            );
                        }
                    )
                    .forEach(
                        function (member) {
                            const privateRoomId =
                                createPrivateRoomId(
                                    project.id,
                                    loginUserNum,
                                    member.userNum
                                );

                            if (!privateRoomId) {
                                return;
                            }

                            const cachedPrivateRoom =
                                roomStateCache.get(
                                    privateRoomId
                                );

                            nextRooms.push({
                                roomId:
                                privateRoomId,

                                roomType:
                                    "PRIVATE",

                                projectId:
                                project.id,

                                roomName:
                                member.name,

                                project:
                                project,

                                targetMember:
                                member,

                                members: [
                                    member
                                ],

                                memberCount:
                                    2,

                                online:
                                    Boolean(
                                        member.online
                                    ),

                                lastMessage:
                                    cachedPrivateRoom
                                        ?.lastMessage ??
                                    null,

                                unreadCount:
                                    cachedPrivateRoom
                                        ?.unreadCount ??
                                    0,

                                updatedAt:
                                    cachedPrivateRoom
                                        ?.lastMessage
                                        ?.createdAt ??
                                    null
                            });
                        }
                    );
            }
        );

        return nextRooms.sort(
            compareChatRooms
        );
    }


    /* =====================================================
       채팅방 목록 출력
    ===================================================== */

    function renderChatRooms() {
        const roomList =
            document.getElementById(
                "chatRoomList"
            );

        if (!roomList) {
            return;
        }

        const filteredRooms =
            getFilteredRooms();

        if (
            filteredRooms.length === 0
        ) {
            roomList.innerHTML = `
                <div class="chat-room-list-empty">
                    표시할 채팅방이 없습니다.
                </div>
            `;

            return;
        }

        roomList.innerHTML =
            filteredRooms
                .map(createRoomItemHtml)
                .join("");
    }


    /* =====================================================
       채팅방 한 개 HTML
    ===================================================== */

    function createRoomItemHtml(room) {
        const isGroup =
            room.roomType === "GROUP";

        const lastMessageText =
            getLastMessageText(room);

        const timeText =
            formatRoomTime(
                room.lastMessage
                    ?.createdAt
            );

        const unreadCount =
            Math.max(
                0,
                Number(
                    room.unreadCount ?? 0
                )
            );

        return `
            <button
                type="button"
                class="chat-room-item"
                data-room-id="${escapeHtml(room.roomId)}"
            >
                <div class="chat-room-avatar-area">
                    ${
            isGroup
                ? createGroupAvatarHtml(
                    room.members
                )
                : createPrivateAvatarHtml(
                    room.targetMember
                )
        }
                </div>

                <div class="chat-room-content">
                    <div class="chat-room-top">
                        <div class="chat-room-name-wrap">
                            <strong class="chat-room-name">
                                ${escapeHtml(room.roomName)}
                            </strong>

                            ${
            isGroup
                ? `
                                        <span class="chat-room-member-count">
                                            ${room.memberCount}
                                        </span>
                                    `
                : `
                                        <span
                                            class="chat-room-online-status
                                                ${room.online ? "online" : "offline"}"
                                        >
                                            ${room.online ? "●" : ""}
                                        </span>
                                    `
        }
                        </div>

                        <time class="chat-room-time">
                            ${escapeHtml(timeText)}
                        </time>
                    </div>

                    <div class="chat-room-bottom">
                        <span class="chat-room-last-message">
                            ${escapeHtml(lastMessageText)}
                        </span>

                        ${
            unreadCount > 0
                ? `
                                    <span class="chat-room-unread">
                                        ${
                    unreadCount > 99
                        ? "99+"
                        : unreadCount
                }
                                    </span>
                                `
                : ""
        }
                    </div>
                </div>
            </button>
        `;
    }


    /* =====================================================
       프로젝트 참여자 아이콘
    ===================================================== */

    function createGroupAvatarHtml(
        members
    ) {
        const displayMembers =
            Array.isArray(members)
                ? members.slice(0, 4)
                : [];

        if (
            displayMembers.length === 0
        ) {
            return `
                <div class="chat-room-avatar chat-room-avatar--default">
                    👥
                </div>
            `;
        }

        return `
            <div class="chat-room-group-avatars">
                ${displayMembers
            .map(
                function (member) {
                    return `
                                <span
                                    class="chat-room-mini-avatar"
                                    title="${escapeHtml(member.name)}"
                                >
                                    ${escapeHtml(
                        getInitial(
                            member.name
                        )
                    )}
                                </span>
                            `;
                }
            )
            .join("")}
            </div>
        `;
    }


    /* =====================================================
       개인 채팅 상대 아이콘
    ===================================================== */

    function createPrivateAvatarHtml(
        member
    ) {
        const name =
            member?.name ??
            "사용자";

        return `
            <div class="chat-room-avatar">
                ${escapeHtml(
            getInitial(name)
        )}
            </div>
        `;
    }


    /* =====================================================
       마지막 메시지 문구
    ===================================================== */

    function getLastMessageText(room) {
        const lastMessage =
            room.lastMessage;

        if (
            !lastMessage ||
            !lastMessage.message
        ) {
            return "아직 대화가 없습니다.";
        }

        const message =
            shortenMessage(
                lastMessage.message,
                30
            );

        if (
            room.roomType === "GROUP"
        ) {
            const senderName =
                lastMessage.senderName ??
                "";

            return senderName
                ? `${senderName}: ${message}`
                : message;
        }

        const senderNum =
            normalizeNumber(
                lastMessage.senderNum
            );

        const isMine =
            senderNum !== null &&
            senderNum ===
            getLoginUserNum();

        return isMine
            ? `나: ${message}`
            : message;
    }


    /* =====================================================
       채팅방 선택
    ===================================================== */

    async function openChatRoom(roomId) {
        const room =
            getRoomById(roomId);

        if (!room) {
            console.warn(
                "채팅방을 찾을 수 없습니다.",
                roomId
            );

            return;
        }

        app.state.chatView =
            "room";

        app.state.currentRoomId =
            room.roomId;

        app.state.currentProjectId =
            room.projectId;

        /*
         * 목록 화면을 숨기고
         * 실제 대화 화면을 표시합니다.
         */
        showRoomView();

        /*
         * 채팅방 제목과 상태를 표시합니다.
         */
        updateRoomHeader(room);

        /*
         * 화면의 안 읽은 메시지 수를 먼저 0으로 변경합니다.
         */
        room.unreadCount = 0;

        saveRoomState(room);

        if (
            room.roomType === "GROUP"
        ) {
            app.state.chatMode =
                "group";

            showGroupFooter();

            /*
             * 프로젝트 단체 채팅방 선택
             */
            if (
                app.chat &&
                typeof app.chat.selectProject ===
                "function"
            ) {
                app.chat.selectProject(
                    room.project
                );
            }

        } else {
            app.state.chatMode =
                "private";

            hideGroupFooter();

            /*
             * 개인 채팅 상대방 선택
             */
            if (
                app.chat &&
                typeof app.chat.selectPrivateMember ===
                "function"
            ) {
                app.chat.selectPrivateMember(
                    room.project,
                    room.targetMember
                );
            }
        }

        /*
         * 프로젝트방과 개인방 모두
         * 서버에 읽음 상태를 저장합니다.
         */
        await markRoomAsRead(
            room.roomId
        );

        /*
         * 읽음 상태가 적용된 목록을 다시 표시합니다.
         */
        renderChatRooms();
    }

    /* =====================================================
       채팅방 목록 화면 표시
    ===================================================== */

    function showChatListView() {
        app.state.chatView =
            "list";

        const listView =
            document.getElementById(
                "chatListView"
            );

        const roomView =
            document.getElementById(
                "chatRoomView"
            );

        if (listView) {
            listView.style.display =
                "flex";
        }

        if (roomView) {
            roomView.style.display =
                "none";
        }

        refreshRooms();
    }

    async function markRoomAsRead(
        roomId
    ) {
        if (
            roomId === null ||
            roomId === undefined ||
            String(roomId).trim() === ""
        ) {
            return false;
        }

        try {
            const encodedRoomId =
                encodeURIComponent(
                    String(roomId).trim()
                );

            const response =
                await fetch(
                    `/api/chat/rooms/${encodedRoomId}/read`,
                    {
                        method: "POST",

                        headers: {
                            "Accept":
                                "application/json"
                        }
                    }
                );

            if (!response.ok) {
                console.warn(
                    "채팅방 읽음 처리 실패:",
                    response.status,
                    roomId
                );

                return false;
            }

            return true;

        } catch (error) {
            console.warn(
                "채팅방 읽음 처리 오류",
                error
            );

            return false;
        }
    }

    /*
     * HTML 뒤로가기 버튼에서 호출됩니다.
     */
    function backToChatList() {
        app.state.chatView =
            "list";

        const listView =
            document.getElementById(
                "chatListView"
            );

        const roomView =
            document.getElementById(
                "chatRoomView"
            );

        if (listView) {
            listView.style.display =
                "flex";
        }

        if (roomView) {
            roomView.style.display =
                "none";
        }

        /*
         * 대화 후 변경된 마지막 메시지와 시간을
         * 다시 정렬해서 출력합니다.
         */
        refreshRooms();
    }


    /* =====================================================
       실제 대화 화면 표시
    ===================================================== */

    function showRoomView() {
        const listView =
            document.getElementById(
                "chatListView"
            );

        const roomView =
            document.getElementById(
                "chatRoomView"
            );

        if (listView) {
            listView.style.display =
                "none";
        }

        if (roomView) {
            roomView.style.display =
                "block";
        }
    }


    /* =====================================================
       채팅방 상단 제목
    ===================================================== */

    function updateRoomHeader(room) {
        const title =
            document.getElementById(
                "chatRoomTitle"
            );

        const subTitle =
            document.getElementById(
                "chatRoomSubTitle"
            );

        if (title) {
            title.textContent =
                room.roomName;
        }

        if (!subTitle) {
            return;
        }

        if (
            room.roomType === "GROUP"
        ) {
            subTitle.textContent =
                `${room.memberCount}명`;

        } else {
            subTitle.textContent =
                room.online
                    ? "접속 중"
                    : "오프라인";
        }
    }


    /* =====================================================
       AI 기능 버튼 표시
    ===================================================== */

    function showGroupFooter() {
        const footer =
            document.querySelector(
                "#chatRoomView .chat-footer"
            );

        if (footer) {
            footer.style.display =
                "";
        }
    }


    function hideGroupFooter() {
        const footer =
            document.querySelector(
                "#chatRoomView .chat-footer"
            );

        if (footer) {
            footer.style.display =
                "none";
        }
    }


    /* =====================================================
       전체 / 프로젝트 / 개인 필터
    ===================================================== */

    function filterRoomList(filter) {
        const normalizedFilter =
            ["all", "group", "private"]
                .includes(filter)
                ? filter
                : "all";

        app.state.roomListFilter =
            normalizedFilter;

        updateFilterButtons();
        renderChatRooms();
    }


    function updateFilterButtons() {
        const buttonMap = {
            all:
                document.getElementById(
                    "allRoomTab"
                ),

            group:
                document.getElementById(
                    "groupRoomTab"
                ),

            private:
                document.getElementById(
                    "privateRoomTab"
                )
        };

        Object.entries(buttonMap).forEach(
            function ([key, button]) {
                if (!button) {
                    return;
                }

                button.classList.toggle(
                    "active",
                    key ===
                    app.state.roomListFilter
                );
            }
        );
    }


    /* =====================================================
       채팅방 검색
    ===================================================== */

    function searchChatRooms() {
        renderChatRooms();
    }


    function getFilteredRooms() {
        const filter =
            app.state.roomListFilter ??
            "all";

        const searchInput =
            document.getElementById(
                "chatRoomSearchInput"
            );

        const keyword =
            String(
                searchInput?.value ??
                ""
            )
                .trim()
                .toLowerCase();

        return chatRooms.filter(
            function (room) {
                const filterMatched =
                    filter === "all" ||
                    (
                        filter === "group" &&
                        room.roomType ===
                        "GROUP"
                    ) ||
                    (
                        filter === "private" &&
                        room.roomType ===
                        "PRIVATE"
                    );

                if (!filterMatched) {
                    return false;
                }

                if (!keyword) {
                    return true;
                }

                const searchableText = [
                    room.roomName,
                    room.project?.name,
                    room.lastMessage
                        ?.message,
                    room.lastMessage
                        ?.senderName
                ]
                    .filter(Boolean)
                    .join(" ")
                    .toLowerCase();

                return searchableText
                    .includes(keyword);
            }
        );
    }


    /* =====================================================
       WebSocket 메시지로 목록 갱신
    ===================================================== */

    function updateRoomFromMessage(
        message
    ) {
        if (
            !message ||
            !message.roomId
        ) {
            return;
        }

        let room =
            getRoomById(
                message.roomId
            );

        /*
         * 아직 목록을 생성하지 않았다면
         * 프로젝트 데이터로 다시 생성합니다.
         */
        if (!room) {
            refreshRooms();

            room =
                getRoomById(
                    message.roomId
                );
        }

        if (!room) {
            return;
        }

        room.lastMessage = {
            senderNum:
                normalizeNumber(
                    message.senderNum
                ),

            senderName:
                String(
                    message.senderName ??
                    ""
                ),

            message:
                String(
                    message.message ??
                    ""
                ),

            createdAt:
                message.createdAt ??
                new Date()
                    .toISOString()
        };

        room.updatedAt =
            room.lastMessage.createdAt;

        const senderNum =
            normalizeNumber(
                message.senderNum
            );

        const isMine =
            senderNum !== null &&
            senderNum ===
            getLoginUserNum();

        const isCurrentOpenRoom =
            app.state.chatView ===
            "room" &&
            String(
                app.state.currentRoomId
            ) ===
            String(
                room.roomId
            );

        if (
            !isMine &&
            !isCurrentOpenRoom
        ) {
            room.unreadCount =
                Number(
                    room.unreadCount ??
                    0
                ) + 1;
        }

        saveRoomState(room);

        chatRooms.sort(
            compareChatRooms
        );

        renderChatRooms();
    }


    function saveRoomState(room) {
        roomStateCache.set(
            room.roomId,
            {
                lastMessage:
                room.lastMessage,

                unreadCount:
                room.unreadCount
            }
        );
    }


    /* =====================================================
       목록 클릭 이벤트
    ===================================================== */

    function bindRoomListEvents() {
        const roomList =
            document.getElementById(
                "chatRoomList"
            );

        if (
            !roomList ||
            roomList.dataset
                .chatListEventBound ===
            "true"
        ) {
            return;
        }

        roomList.dataset
            .chatListEventBound =
            "true";

        roomList.addEventListener(
            "click",
            function (event) {
                const roomItem =
                    event.target.closest(
                        "[data-room-id]"
                    );

                if (!roomItem) {
                    return;
                }

                openChatRoom(
                    roomItem.dataset.roomId
                );
            }
        );
    }


    /* =====================================================
       프로젝트 데이터 조회
    ===================================================== */

    function getProjects() {
        if (
            app.project &&
            typeof app.project
                .getProjects ===
            "function"
        ) {
            return app.project
                .getProjects();
        }

        return [];
    }


    function getRooms() {
        return [...chatRooms];
    }


    function getRoomById(roomId) {
        return (
            chatRooms.find(
                function (room) {
                    return (
                        String(room.roomId) ===
                        String(roomId)
                    );
                }
            ) ?? null
        );
    }


    /* =====================================================
       개인 채팅방 ID 생성
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
       채팅방 정렬
    ===================================================== */

    function compareChatRooms(
        first,
        second
    ) {
        const firstTime =
            getTimestamp(
                first.updatedAt
            );

        const secondTime =
            getTimestamp(
                second.updatedAt
            );

        if (
            firstTime !== secondTime
        ) {
            return (
                secondTime -
                firstTime
            );
        }

        /*
         * 마지막 메시지가 없는 경우
         * 프로젝트 채팅을 개인 채팅보다 위에 표시합니다.
         */
        if (
            first.roomType !==
            second.roomType
        ) {
            return first.roomType ===
            "GROUP"
                ? -1
                : 1;
        }

        return first.roomName
            .localeCompare(
                second.roomName,
                "ko"
            );
    }


    /* =====================================================
       시간 표시
    ===================================================== */

    function formatRoomTime(
        createdAt
    ) {
        if (!createdAt) {
            return "";
        }

        const date =
            new Date(createdAt);

        if (
            Number.isNaN(
                date.getTime()
            )
        ) {
            return "";
        }

        const now =
            new Date();

        const sameDate =
            date.getFullYear() ===
            now.getFullYear() &&
            date.getMonth() ===
            now.getMonth() &&
            date.getDate() ===
            now.getDate();

        if (sameDate) {
            return date
                .toLocaleTimeString(
                    "ko-KR",
                    {
                        hour: "numeric",
                        minute: "2-digit"
                    }
                );
        }

        const sameYear =
            date.getFullYear() ===
            now.getFullYear();

        if (sameYear) {
            return `${date.getMonth() + 1}.${date.getDate()}`;
        }

        return [
            date.getFullYear(),
            String(
                date.getMonth() + 1
            ).padStart(2, "0"),
            String(
                date.getDate()
            ).padStart(2, "0")
        ].join(".");
    }


    function getTimestamp(value) {
        if (!value) {
            return 0;
        }

        const timestamp =
            new Date(value)
                .getTime();

        return Number.isNaN(timestamp)
            ? 0
            : timestamp;
    }


    /* =====================================================
       공통 함수
    ===================================================== */

    function getLoginUserNum() {
        return normalizeNumber(
            app.state
                ?.loginUser
                ?.userNum ??
            window.currentUserNum ??
            window.currentUserId
        );
    }


    function getInitial(name) {
        const text =
            String(name ?? "")
                .trim();

        return text
            ? text.charAt(0)
            : "?";
    }


    function shortenMessage(
        message,
        maxLength
    ) {
        const text =
            String(message ?? "");

        return text.length <= maxLength
            ? text
            : `${text.substring(
                0,
                maxLength
            )}…`;
    }


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


    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    /* =====================================================
   서버 메시지와 브라우저 캐시 중 최신 메시지 선택
===================================================== */

    function getNewerMessage(
        serverMessage,
        cachedMessage
    ) {
        if (!serverMessage) {
            return cachedMessage;
        }

        if (!cachedMessage) {
            return serverMessage;
        }

        const serverTime =
            getTimestamp(
                serverMessage.createdAt
            );

        const cachedTime =
            getTimestamp(
                cachedMessage.createdAt
            );

        return cachedTime > serverTime
            ? cachedMessage
            : serverMessage;
    }


    /* =====================================================
       개인 채팅 상대방 찾기
    ===================================================== */

    function findTargetMember(
        project,
        roomId
    ) {
        if (
            !project ||
            !Array.isArray(
                project.members
            )
        ) {
            return null;
        }

        const roomParts =
            String(roomId)
                .split("_");

        if (
            roomParts.length < 5
        ) {
            return null;
        }

        const firstUserNum =
            normalizeNumber(
                roomParts[
                roomParts.length - 2
                    ]
            );

        const secondUserNum =
            normalizeNumber(
                roomParts[
                roomParts.length - 1
                    ]
            );

        const loginUserNum =
            getLoginUserNum();

        const targetUserNum =
            firstUserNum === loginUserNum
                ? secondUserNum
                : firstUserNum;

        if (targetUserNum === null) {
            return null;
        }

        return (
            project.members.find(
                function (member) {
                    return (
                        normalizeNumber(
                            member.userNum
                        ) === targetUserNum
                    );
                }
            ) ?? null
        );
    }


    /* =====================================================
       채팅방 목록 로딩 표시
    ===================================================== */

    function renderRoomListLoading() {
        const roomList =
            document.getElementById(
                "chatRoomList"
            );

        if (!roomList) {
            return;
        }

        /*
         * 이미 채팅방이 출력되어 있다면
         * 다시 조회하는 동안 기존 화면을 유지합니다.
         */
        if (chatRooms.length > 0) {
            return;
        }

        roomList.innerHTML = `
            <div class="chat-room-list-empty">
                채팅방을 불러오는 중입니다.
            </div>
        `;
    }


    /* =====================================================
       외부 공개 함수
    ===================================================== */

    app.chatList = {
        refreshRooms,
        renderChatRooms,

        showChatListView,
        showRoomView,
        backToChatList,
        openChatRoom,

        filterRoomList,
        searchChatRooms,

        updateRoomFromMessage,

        getRooms,
        getRoomById
    };


    /* =====================================================
       HTML onclick 호환
    ===================================================== */

    window.backToChatList =
        backToChatList;

    window.filterRoomList =
        filterRoomList;

    window.searchChatRooms =
        searchChatRooms;

    window.openChatRoom =
        openChatRoom;


    /* =====================================================
       페이지 로드 후 실행
    ===================================================== */

    if (
        document.readyState ===
        "loading"
    ) {
        document.addEventListener(
            "DOMContentLoaded",
            initializeChatList
        );

    } else {
        initializeChatList();
    }

})(
    window.WorkTopusChat =
        window.WorkTopusChat || {}
);