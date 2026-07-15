(function (app) {
    "use strict";

    /* =====================================================
       임시 로그인 사용자

       실제 로그인 정보가 window.loginUser에 존재하면
       해당 정보를 사용합니다.
    ===================================================== */

    window.loginUser = window.loginUser || {
        userNum: 1,
        userId: "testuser",
        name: "신승민"
    };


    /* =====================================================
       공통 상태
    ===================================================== */

    app.state = app.state || {};

    app.state.loginUser = app.state.loginUser || {
        userNum: normalizeNumber(
            window.loginUser.userNum
        ),

        userId: String(
            window.loginUser.userId ?? ""
        ),

        name: String(
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

        chatMode: "group"
    };

    Object.entries(defaultState).forEach(
        function ([key, value]) {
            if (app.state[key] === undefined) {
                app.state[key] = value;
            }
        }
    );


    /* =====================================================
       설정
    ===================================================== */

    const PROJECT_API = "/api/chat/projects";

    let projects = [];
    let searchKeyword = "";

    const openedProjectIds = new Set();


    /* =====================================================
       로그인 사용자 갱신
    ===================================================== */

    function setLoginUser(loginUser) {
        if (!loginUser) {
            return;
        }

        app.state.loginUser = {
            userNum: normalizeNumber(
                loginUser.userNum
            ),

            userId: String(
                loginUser.userId ?? ""
            ),

            name: String(
                loginUser.name ??
                loginUser.userName ??
                ""
            )
        };

        window.loginUser = app.state.loginUser;
    }


    /* =====================================================
       현재 로그인 사용자 조회
    ===================================================== */

    async function loadLoginUser() {
        const response = await fetch("/api/chat/me", {
            method: "GET",
            headers: {
                "Accept": "application/json"
            }
        });

        if (!response.ok) {
            throw new Error(`로그인 사용자 조회 실패: ${response.status}`);
        }

        const loginUser = await response.json();
        setLoginUser(loginUser);
        return loginUser;
    }


    /* =====================================================
       프로젝트 목록 조회
    ===================================================== */

    async function loadProjects() {
        renderLoading();

        try {
            await loadLoginUser();
            const response = await fetch(
                PROJECT_API,
                {
                    method: "GET",

                    headers: {
                        "Accept": "application/json"
                    }
                }
            );

            if (!response.ok) {
                throw new Error(
                    `프로젝트 조회 실패: ${response.status}`
                );
            }

            const data =
                await response.json();

            projects = extractProjectList(data)
                .map(normalizeProject)
                .filter(
                    function (project) {
                        return project.id !== null;
                    }
                );

            sortProjects();
            initializeCurrentProject();

            /*
             * 예전 projectList가 있는 화면에서는
             * 기존 프로젝트 목록도 출력합니다.
             */
            renderProjects();
            updateCurrentProjectTitle();

            /*
             * 새 채팅방 목록 화면을 갱신합니다.
             */
            if (
                app.chatList &&
                typeof app.chatList.refreshRooms ===
                "function"
            ) {
                app.chatList.refreshRooms();
            }

            return getProjects();

        } catch (error) {
            console.error(
                "프로젝트 목록 조회 오류",
                error
            );

            projects = [];

            clearCurrentProject();
            renderProjectError();

            if (
                app.chatList &&
                typeof app.chatList.renderChatRooms ===
                "function"
            ) {
                app.chatList.renderChatRooms();
            }

            return [];
        }
    }


    /* =====================================================
       프로젝트 목록 응답 추출
    ===================================================== */

    function extractProjectList(data) {
        if (Array.isArray(data)) {
            return data;
        }

        if (
            data &&
            Array.isArray(data.projects)
        ) {
            return data.projects;
        }

        if (
            data &&
            Array.isArray(data.content)
        ) {
            return data.content;
        }

        return [];
    }


    /* =====================================================
       현재 프로젝트 초기화
    ===================================================== */

    function initializeCurrentProject() {
        if (projects.length === 0) {
            clearCurrentProject();
            return;
        }

        const selectedProject =
            getProjectById(
                app.state.currentProjectId
            );

        if (selectedProject) {
            app.state.currentProjectName =
                selectedProject.name;

            app.state.currentRoomId =
                selectedProject.roomId;

            openedProjectIds.add(
                selectedProject.id
            );

            return;
        }

        const firstProject = projects[0];

        app.state.currentProjectId =
            firstProject.id;

        app.state.currentProjectName =
            firstProject.name;

        app.state.currentRoomId =
            firstProject.roomId;

        app.state.currentPrivateUserNum =
            null;

        app.state.currentPrivateMemberName =
            "";

        app.state.chatMode = "group";

        openedProjectIds.add(
            firstProject.id
        );
    }


    function clearCurrentProject() {
        app.state.currentProjectId = null;
        app.state.currentProjectName = "";
        app.state.currentRoomId = null;

        app.state.currentPrivateUserNum = null;
        app.state.currentPrivateMemberName = "";

        app.state.chatMode = "group";
    }


    /* =====================================================
       프로젝트 데이터 정리
    ===================================================== */

    function normalizeProject(project) {
        const projectId = normalizeNumber(
            project.projectId ??
            project.id
        );

        const projectName = String(
            project.projectName ??
            project.name ??
            "이름 없는 프로젝트"
        );

        const roomId = String(
            project.groupRoomId ??
            project.roomId ??
            createGroupRoomId(projectId)
        );

        const memberList =
            project.members ??
            project.projectMembers ??
            project.memberList ??
            [];

        return {
            id: projectId,

            projectId: projectId,

            name: projectName,

            description: String(
                project.description ??
                project.projectDescription ??
                ""
            ),

            roomId: roomId,

            unreadCount: Math.max(
                0,
                normalizeNumber(
                    project.unreadCount ??
                    project.unread ??
                    0,
                    0
                )
            ),

            lastMessage: normalizeLastMessage(
                project.lastMessage
            ),

            members: Array.isArray(memberList)
                ? memberList
                    .map(normalizeMember)
                    .filter(function (member) {
                        return member.userNum !== null;
                    })
                : []
        };
    }


    /* =====================================================
       프로젝트 팀원 데이터 정리

       owner = 프로젝트 팀장 여부
    ===================================================== */

    function normalizeMember(member) {
        const userNum = normalizeNumber(
            member.userNum ??
            member.memberId ??
            member.id
        );

        const role = String(
            member.role ??
            member.projectRole ??
            ""
        ).toUpperCase();

        return {
            userNum: userNum,

            /*
             * 기존 코드 호환용
             */
            id: userNum,

            userId: String(
                member.userId ?? ""
            ),

            name: String(
                member.userName ??
                member.memberName ??
                member.name ??
                "이름 없음"
            ),

            online: normalizeBoolean(
                member.online ??
                member.isOnline ??
                member.onlineYn ??
                false
            ),

            /*
             * 프로젝트 팀장 여부
             *
             * 다음 응답 형식을 모두 지원합니다.
             *
             * owner: true
             * isOwner: true
             * ownerYn: "Y"
             * role: "OWNER"
             */
            owner: normalizeBoolean(
                member.owner ??
                member.isOwner ??
                member.ownerYn ??
                (role === "OWNER")
            )
        };
    }


    /* =====================================================
       마지막 메시지 데이터 정리
    ===================================================== */

    function normalizeLastMessage(lastMessage) {
        if (!lastMessage) {
            return null;
        }

        return {
            senderNum: normalizeNumber(
                lastMessage.senderNum ??
                lastMessage.senderUserNum ??
                lastMessage.senderId ??
                null
            ),

            senderName: String(
                lastMessage.senderName ??
                lastMessage.sender ??
                lastMessage.userName ??
                ""
            ),

            message: String(
                lastMessage.message ??
                lastMessage.content ??
                ""
            ),

            createdAt:
                lastMessage.createdAt ??
                lastMessage.sentAt ??
                lastMessage.regDate ??
                lastMessage.time ??
                null
        };
    }


    /* =====================================================
       프로젝트 목록 출력
    ===================================================== */

    function renderProjects() {
        const projectList =
            document.getElementById("projectList");

        if (!projectList) {
            return;
        }

        const filteredProjects =
            getFilteredProjects();

        if (filteredProjects.length === 0) {
            projectList.innerHTML = `
                <div class="project-empty">
                    표시할 프로젝트가 없습니다.
                </div>
            `;

            bindProjectEvents();
            return;
        }

        projectList.innerHTML =
            filteredProjects
                .map(createProjectHtml)
                .join("");

        bindProjectEvents();
    }


    /* =====================================================
       프로젝트 한 개 HTML
    ===================================================== */

    function createProjectHtml(project) {
        const isOpened =
            openedProjectIds.has(project.id);

        const isSelected =
            Number(app.state.currentProjectId) ===
            project.id;

        const onlineCount =
            project.members.filter(
                function (member) {
                    return member.online;
                }
            ).length;

        return `
            <div
                id="project-box-${project.id}"
                class="project-box
                    ${isOpened ? "open" : ""}
                    ${isSelected ? "active" : ""}"
                data-project-id="${project.id}"
            >
                <div
                    class="project-header"
                    data-action="select-project"
                    data-project-id="${project.id}"
                    role="button"
                    tabindex="0"
                >
                    <span
                        class="project-arrow"
                        aria-hidden="true"
                    >
                        ${isOpened ? "▼" : "▶"}
                    </span>

                    <div class="project-info">
                        <div class="project-title">
                            📁 ${escapeHtml(project.name)}
                        </div>

                        <div class="project-count">
                            👥 ${project.members.length}명
                            &nbsp;&nbsp;
                            🟢 ${onlineCount}명 접속
                        </div>
                    </div>

                    ${createUnreadBadge(project)}
                </div>

                <div class="project-members">
                    ${createMemberListHtml(project)}
                    ${createLastMessageHtml(project)}
                </div>
            </div>
        `;
    }


    /* =====================================================
       프로젝트 팀원 HTML

       출력 순서:
       1. 접속 중인 팀원
       2. 같은 상태라면 owner
       3. 같은 상태라면 현재 사용자
       4. 이름 가나다순
    ===================================================== */

    function createMemberListHtml(project) {
        if (project.members.length === 0) {
            return `
                <div class="project-member-empty">
                    등록된 팀원이 없습니다.
                </div>
            `;
        }

        const loginUserNum =
            app.state.loginUser.userNum;

        const sortedMembers =
            [...project.members].sort(
                function (first, second) {
                    /*
                     * 접속 중인 사용자 우선
                     */
                    if (
                        first.online !==
                        second.online
                    ) {
                        return (
                            Number(second.online) -
                            Number(first.online)
                        );
                    }

                    /*
                     * 접속 상태가 같으면 owner 우선
                     */
                    if (
                        first.owner !==
                        second.owner
                    ) {
                        return (
                            Number(second.owner) -
                            Number(first.owner)
                        );
                    }

                    /*
                     * 접속 상태와 owner 여부가 같으면
                     * 현재 로그인 사용자 우선
                     */
                    const firstIsMe =
                        first.userNum ===
                        loginUserNum;

                    const secondIsMe =
                        second.userNum ===
                        loginUserNum;

                    if (firstIsMe !== secondIsMe) {
                        return (
                            Number(secondIsMe) -
                            Number(firstIsMe)
                        );
                    }

                    /*
                     * 마지막은 이름순
                     */
                    return first.name.localeCompare(
                        second.name,
                        "ko"
                    );
                }
            );

        return sortedMembers
            .map(function (member) {
                const isCurrentUser =
                    member.userNum ===
                    loginUserNum;

                return `
                    <div
                        class="member-item
                            ${isCurrentUser
                    ? "member-item--me"
                    : ""}"
                        data-action="select-member"
                        data-project-id="${project.id}"
                        data-user-num="${member.userNum}"
                        role="button"
                        tabindex="${isCurrentUser ? "-1" : "0"}"
                        aria-disabled="${isCurrentUser}"
                    >
                        <span
                            class="member-status
                                ${member.online
                    ? "online"
                    : "offline"}"
                        >
                            ${member.online ? "🟢" : "⚪"}
                        </span>

                        <span class="member-name">
                            ${escapeHtml(member.name)}
                        </span>

                        ${
                    member.owner
                        ? `
                                    <span
                                        class="member-leader"
                                        title="프로젝트 팀장"
                                    >
                                        ⭐
                                    </span>
                                `
                        : ""
                }

                        ${
                    isCurrentUser
                        ? `
                                    <span class="member-me">
                                        나
                                    </span>
                                `
                        : ""
                }
                    </div>
                `;
            })
            .join("");
    }


    /* =====================================================
       마지막 메시지 HTML
    ===================================================== */

    function createLastMessageHtml(project) {
        const lastMessage =
            project.lastMessage;

        if (
            !lastMessage ||
            !lastMessage.message
        ) {
            return `
                <div class="last-message">
                    <div class="last-message-text">
                        아직 대화가 없습니다.
                    </div>
                </div>
            `;
        }

        return `
            <div class="last-message">
                <div class="last-message-text">
                    💬
                    ${escapeHtml(lastMessage.senderName)}
                    :
                    ${escapeHtml(
            shortenMessage(
                lastMessage.message
            )
        )}
                </div>

                <div class="last-message-time">
                    ${escapeHtml(
            formatMessageTime(
                lastMessage.createdAt
            )
        )}
                </div>
            </div>
        `;
    }


    /* =====================================================
       안 읽은 메시지 배지
    ===================================================== */

    function createUnreadBadge(project) {
        if (project.unreadCount <= 0) {
            return "";
        }

        const displayCount =
            project.unreadCount > 99
                ? "99+"
                : project.unreadCount;

        return `
            <span
                id="unread-${project.id}"
                class="unread-message"
            >
                ${displayCount}
            </span>
        `;
    }


    /* =====================================================
       프로젝트 이벤트 등록
    ===================================================== */

    function bindProjectEvents() {
        const projectList =
            document.getElementById("projectList");

        if (
            projectList &&
            projectList.dataset.eventBound !== "true"
        ) {
            projectList.dataset.eventBound = "true";

            projectList.addEventListener(
                "click",
                handleProjectListClick
            );

            projectList.addEventListener(
                "keydown",
                handleProjectListKeydown
            );
        }

        const searchInput =
            document.getElementById("projectSearch");

        if (
            searchInput &&
            searchInput.dataset.eventBound !== "true"
        ) {
            searchInput.dataset.eventBound = "true";

            searchInput.addEventListener(
                "input",
                handleProjectSearch
            );
        }
    }


    function handleProjectListClick(event) {
        const target =
            event.target.closest("[data-action]");

        if (!target) {
            return;
        }

        const action =
            target.dataset.action;

        if (action === "select-project") {
            handleProjectClick(
                normalizeNumber(
                    target.dataset.projectId
                )
            );

            return;
        }

        if (action === "select-member") {
            selectProjectMember(
                normalizeNumber(
                    target.dataset.projectId
                ),

                normalizeNumber(
                    target.dataset.userNum
                )
            );
        }
    }


    function handleProjectListKeydown(event) {
        if (
            event.key !== "Enter" &&
            event.key !== " "
        ) {
            return;
        }

        const target =
            event.target.closest("[data-action]");

        if (!target) {
            return;
        }

        event.preventDefault();
        target.click();
    }


    function handleProjectSearch(event) {
        searchKeyword =
            event.target.value
                .trim()
                .toLowerCase();

        renderProjects();
    }


    /* =====================================================
       프로젝트 클릭
    ===================================================== */

    function handleProjectClick(projectId) {
        if (projectId === null) {
            return;
        }

        if (openedProjectIds.has(projectId)) {
            openedProjectIds.delete(projectId);

        } else {
            openedProjectIds.add(projectId);
        }

        selectProject(projectId);
    }


    /* =====================================================
       프로젝트 선택
    ===================================================== */

    function selectProject(projectId) {
        const project =
            getProjectById(projectId);

        if (!project) {
            console.error(
                "프로젝트를 찾을 수 없습니다.",
                projectId
            );

            return;
        }

        app.state.currentProjectId =
            project.id;

        app.state.currentProjectName =
            project.name;

        app.state.currentRoomId =
            project.roomId;

        app.state.currentPrivateUserNum =
            null;

        app.state.currentPrivateMemberName =
            "";

        app.state.chatMode = "group";

        clearProjectUnread(project.id);

        updateCurrentProjectTitle();
        renderProjects();

        if (
            app.chat &&
            typeof app.chat.selectProject ===
            "function"
        ) {
            app.chat.selectProject(project);
        }

        markProjectAsRead(project.id);
    }


    /* =====================================================
       프로젝트 펼치기 / 접기
    ===================================================== */

    function toggleProject(projectId) {
        const normalizedProjectId =
            normalizeNumber(projectId);

        if (normalizedProjectId === null) {
            return;
        }

        if (
            openedProjectIds.has(
                normalizedProjectId
            )
        ) {
            openedProjectIds.delete(
                normalizedProjectId
            );

        } else {
            openedProjectIds.add(
                normalizedProjectId
            );
        }

        renderProjects();
    }


    /* =====================================================
       개인 채팅 상대 선택
    ===================================================== */

    function selectProjectMember(
        projectId,
        userNum
    ) {
        const project =
            getProjectById(projectId);

        if (!project) {
            return;
        }

        const member =
            project.members.find(
                function (item) {
                    return item.userNum === userNum;
                }
            );

        if (!member) {
            return;
        }

        /*
         * 자신과는 개인 채팅을 열지 않습니다.
         */
        if (
            member.userNum ===
            app.state.loginUser.userNum
        ) {
            return;
        }

        app.state.currentProjectId =
            project.id;

        app.state.currentProjectName =
            project.name;

        app.state.currentPrivateUserNum =
            member.userNum;

        app.state.currentPrivateMemberName =
            member.name;

        app.state.chatMode = "private";

        if (
            app.chat &&
            typeof app.chat.selectPrivateMember ===
            "function"
        ) {
            app.chat.selectPrivateMember(
                project,
                member
            );
        }
    }


    /* =====================================================
       실시간 마지막 메시지 갱신
    ===================================================== */

    function updateProjectLastMessage(message) {
        if (!message) {
            return;
        }

        const messageProjectId =
            normalizeNumber(
                message.projectId
            );

        const messageRoomId =
            message.roomId !== undefined &&
            message.roomId !== null
                ? String(message.roomId)
                : null;

        const project =
            projects.find(
                function (item) {
                    if (
                        messageProjectId !== null &&
                        item.id === messageProjectId
                    ) {
                        return true;
                    }

                    return (
                        messageRoomId !== null &&
                        item.roomId === messageRoomId
                    );
                }
            );

        if (!project) {
            return;
        }

        const senderNum =
            normalizeNumber(
                message.senderNum ??
                message.senderUserNum ??
                message.senderId
            );

        const senderName =
            String(
                message.senderName ??
                message.sender ??
                message.userName ??
                ""
            );

        project.lastMessage = {
            senderNum: senderNum,

            senderName: senderName,

            message: String(
                message.message ??
                message.content ??
                ""
            ),

            createdAt:
                message.createdAt ??
                message.sentAt ??
                new Date().toISOString()
        };

        const isOwnMessage =
            senderNum !== null
                ? senderNum ===
                app.state.loginUser.userNum
                : (
                    senderName !== "" &&
                    senderName ===
                    app.state.loginUser.name
                );

        const isCurrentRoom =
            String(
                app.state.currentRoomId ?? ""
            ) ===
            String(
                messageRoomId ??
                project.roomId
            );

        if (
            !isOwnMessage &&
            (
                !isCurrentRoom ||
                !isChatModalOpen()
            )
        ) {
            project.unreadCount += 1;
        }

        sortProjects();
        renderProjects();
    }


    /* =====================================================
       실시간 접속 상태 갱신
    ===================================================== */

    function updateMemberOnlineStatus(status) {
        if (!status) {
            return;
        }

        const projectId =
            normalizeNumber(
                status.projectId
            );

        const userNum =
            normalizeNumber(
                status.userNum ??
                status.memberId ??
                status.id
            );

        if (userNum === null) {
            return;
        }

        const targetProjects =
            projectId !== null
                ? projects.filter(
                    function (project) {
                        return (
                            project.id === projectId
                        );
                    }
                )
                : projects;

        targetProjects.forEach(
            function (project) {
                const member =
                    project.members.find(
                        function (item) {
                            return (
                                item.userNum ===
                                userNum
                            );
                        }
                    );

                if (member) {
                    member.online =
                        normalizeBoolean(
                            status.online ??
                            status.isOnline ??
                            status.onlineYn
                        );
                }
            }
        );

        /*
         * 다시 출력하면서 접속 중인 사용자가
         * 위쪽으로 자동 정렬됩니다.
         */
        renderProjects();
    }


    /* =====================================================
       안 읽은 메시지 초기화
    ===================================================== */

    function clearProjectUnread(projectId) {
        const project =
            getProjectById(projectId);

        if (project) {
            project.unreadCount = 0;
        }
    }


    /* =====================================================
       프로젝트 읽음 처리
    ===================================================== */

    async function markProjectAsRead(projectId) {
        try {
            const response = await fetch(
                `${PROJECT_API}/${projectId}/read`,
                {
                    method: "POST",

                    headers: {
                        "Accept": "application/json"
                    }
                }
            );

            if (!response.ok) {
                console.warn(
                    "프로젝트 읽음 처리 실패:",
                    response.status
                );
            }

        } catch (error) {
            console.warn(
                "프로젝트 읽음 처리 요청 오류",
                error
            );
        }
    }


    /* =====================================================
       프로젝트 데이터 반환
    ===================================================== */

    function getProjects() {
        return [...projects];
    }


    function getProjectById(projectId) {
        const normalizedProjectId =
            normalizeNumber(projectId);

        if (normalizedProjectId === null) {
            return null;
        }

        return (
            projects.find(
                function (project) {
                    return (
                        project.id ===
                        normalizedProjectId
                    );
                }
            ) ?? null
        );
    }


    function getCurrentProject() {
        return getProjectById(
            app.state.currentProjectId
        );
    }


    function replaceProjects(newProjects) {
        projects =
            Array.isArray(newProjects)
                ? newProjects
                    .map(normalizeProject)
                    .filter(
                        function (project) {
                            return project.id !== null;
                        }
                    )
                : [];

        sortProjects();
        initializeCurrentProject();

        renderProjects();
        updateCurrentProjectTitle();
    }


    /* =====================================================
       프로젝트 검색
    ===================================================== */

    function getFilteredProjects() {
        if (!searchKeyword) {
            return projects;
        }

        return projects.filter(
            function (project) {
                return (
                    project.name
                        .toLowerCase()
                        .includes(searchKeyword) ||
                    project.description
                        .toLowerCase()
                        .includes(searchKeyword)
                );
            }
        );
    }


    /* =====================================================
       최근 메시지 순 프로젝트 정렬
    ===================================================== */

    function sortProjects() {
        projects.sort(
            function (
                firstProject,
                secondProject
            ) {
                return (
                    getMessageTimestamp(
                        secondProject.lastMessage
                    ) -
                    getMessageTimestamp(
                        firstProject.lastMessage
                    )
                );
            }
        );
    }


    /* =====================================================
       채팅 제목 변경
    ===================================================== */

    function updateCurrentProjectTitle() {
        const title =
            document.getElementById("chatTitle");

        if (!title) {
            return;
        }

        title.textContent =
            app.state.currentProjectName ||
            "프로젝트 채팅";
    }


    /* =====================================================
       채팅 팝업 열림 여부
    ===================================================== */

    function isChatModalOpen() {
        const modal =
            document.getElementById("chatModal");

        if (!modal) {
            return false;
        }

        const style =
            window.getComputedStyle(modal);

        return (
            style.display !== "none" &&
            style.visibility !== "hidden"
        );
    }


    /* =====================================================
       로딩 화면
    ===================================================== */

    function renderLoading() {
        const projectList =
            document.getElementById("projectList");

        if (!projectList) {
            return;
        }

        projectList.innerHTML = `
            <div class="project-loading">
                프로젝트를 불러오는 중입니다.
            </div>
        `;
    }


    /* =====================================================
       프로젝트 조회 오류
    ===================================================== */

    function renderProjectError() {
        const projectList =
            document.getElementById("projectList");

        if (!projectList) {
            return;
        }

        projectList.innerHTML = `
            <div class="project-error">
                <p>
                    프로젝트를 불러오지 못했습니다.
                </p>

                <button
                    type="button"
                    id="projectReloadButton"
                >
                    다시 불러오기
                </button>
            </div>
        `;

        const reloadButton =
            document.getElementById(
                "projectReloadButton"
            );

        if (reloadButton) {
            reloadButton.addEventListener(
                "click",
                loadProjects
            );
        }
    }


    /* =====================================================
       단체 채팅방 ID 생성
    ===================================================== */

    function createGroupRoomId(projectId) {
        return `project_${projectId}_group`;
    }


    /* =====================================================
       마지막 메시지 길이 제한
    ===================================================== */

    function shortenMessage(message) {
        const text =
            String(message ?? "");

        const maxLength = 20;

        return text.length <= maxLength
            ? text
            : `${text.substring(
                0,
                maxLength
            )}...`;
    }


    /* =====================================================
       메시지 시간 정렬값
    ===================================================== */

    function getMessageTimestamp(lastMessage) {
        if (
            !lastMessage ||
            !lastMessage.createdAt
        ) {
            return 0;
        }

        const timestamp =
            new Date(
                lastMessage.createdAt
            ).getTime();

        return Number.isNaN(timestamp)
            ? 0
            : timestamp;
    }


    /* =====================================================
       메시지 시간 표시
    ===================================================== */

    function formatMessageTime(createdAt) {
        if (!createdAt) {
            return "";
        }

        const date =
            new Date(createdAt);

        if (Number.isNaN(date.getTime())) {
            return String(createdAt);
        }

        const today = new Date();

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
                day: "numeric"
            }
        );
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

        const number = Number(value);

        return Number.isFinite(number)
            ? number
            : defaultValue;
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
            normalizedValue === "online" ||
            normalizedValue === "owner"
        );
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
       외부 공개 함수
    ===================================================== */

    app.project = {
        loadProjects,
        renderProjects,

        selectProject,
        selectProjectMember,
        toggleProject,

        getProjects,
        getProjectById,
        getCurrentProject,
        replaceProjects,

        updateProjectLastMessage,
        updateMemberOnlineStatus,

        clearProjectUnread,
        setLoginUser
    };


    /* =====================================================
       기존 코드 호환
    ===================================================== */

    window.loadProjects =
        loadProjects;

    window.selectProject =
        selectProject;

    window.selectProjectMember =
        selectProjectMember;

    window.toggleProject =
        toggleProject;

    window.updateProjectLastMessage =
        updateProjectLastMessage;

})(
    window.WorkTopusChat =
        window.WorkTopusChat || {}
);