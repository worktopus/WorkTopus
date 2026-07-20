(function (app) {
    "use strict";


    /* =====================================================
       팀원 조회 API
    ===================================================== */

    const MEMBER_API =
        "/member";


    /* =====================================================
       프로젝트별 팀원 캐시
    ===================================================== */

    const membersByProject =
        new Map();

    let loadingProjectId =
        null;


    /* =====================================================
       프로젝트 팀원 조회
    ===================================================== */

    async function loadTeamMembers(
        projectId,
        forceReload = false
    ) {
        const normalizedProjectId =
            normalizeNumber(
                projectId ??
                app.state?.currentProjectId
            );

        if (
            normalizedProjectId === null
        ) {
            console.warn(
                "팀원을 조회할 projectId가 없습니다."
            );

            renderMemberEmpty(
                "프로젝트를 먼저 선택하세요."
            );

            return [];
        }

        /*
        project.js가 이미 프로젝트와 팀원 정보를
        내려받았다면 해당 정보를 먼저 사용합니다.
        */
        if (!forceReload) {
            const projectMembers =
                getMembersFromProject(
                    normalizedProjectId
                );

            if (
                projectMembers.length > 0
            ) {
                setProjectMembers(
                    normalizedProjectId,
                    projectMembers
                );

                renderMembers(
                    normalizedProjectId
                );

                return projectMembers;
            }

            const cachedMembers =
                getMembers(
                    normalizedProjectId
                );

            if (
                cachedMembers.length > 0
            ) {
                renderMembers(
                    normalizedProjectId
                );

                return cachedMembers;
            }
        }

        loadingProjectId =
            normalizedProjectId;

        renderMemberLoading();

        try {
            /*
            현재 로그인 사용자는 서버 세션이나
            Spring Security에서 직접 확인합니다.

            userNum을 쿼리 파라미터로 보내지 않습니다.
            */
            const response =
                await fetch(
                    `${MEMBER_API}/${encodeURIComponent(normalizedProjectId)}`,
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
                    `팀원 조회 실패: ${response.status}`
                );
            }

            const data =
                await response.json();

            const members =
                normalizeMemberResponse(
                    data
                );

            setProjectMembers(
                normalizedProjectId,
                members
            );

            /*
            조회 중 다른 프로젝트로 이동했다면
            이전 프로젝트 팀원을 현재 화면에 출력하지 않습니다.
            */
            if (
                Number(
                    app.state
                        ?.currentProjectId
                ) === normalizedProjectId
            ) {
                renderMembers(
                    normalizedProjectId
                );
            }

            return members;

        } catch (error) {
            console.error(
                "프로젝트 팀원 조회 오류",
                error
            );

            if (
                Number(
                    app.state
                        ?.currentProjectId
                ) === normalizedProjectId
            ) {
                renderMemberError(
                    normalizedProjectId
                );
            }

            return [];

        } finally {
            if (
                loadingProjectId ===
                normalizedProjectId
            ) {
                loadingProjectId =
                    null;
            }
        }
    }


    /* =====================================================
       서버 응답에서 팀원 목록 추출
    ===================================================== */

    function normalizeMemberResponse(
        data
    ) {
        let memberList = [];

        if (Array.isArray(data)) {
            memberList =
                data;

        } else if (
            data &&
            Array.isArray(data.members)
        ) {
            memberList =
                data.members;

        } else if (
            data &&
            Array.isArray(data.content)
        ) {
            memberList =
                data.content;

        } else if (
            data &&
            Array.isArray(
                data.projectMembers
            )
        ) {
            memberList =
                data.projectMembers;
        }

        return memberList
            .map(normalizeMember)
            .filter(
                function (member) {
                    return (
                        member.userNum !==
                        null
                    );
                }
            );
    }


    /* =====================================================
       팀원 데이터 정리

       userNum : USERS 숫자 PK
       userId  : 로그인 아이디
       name    : 화면 표시 이름
    ===================================================== */

    function normalizeMember(member) {
        const userNum =
            normalizeNumber(
                member.userNum ??
                member.memberId ??
                member.id
            );

        return {
            userNum:
            userNum,

            /*
            기존 코드 호환용 별칭입니다.
            새 코드에서는 userNum을 사용합니다.
            */
            id:
            userNum,

            userId:
                String(
                    member.userId ??
                    ""
                ),

            name:
                String(
                    member.userName ??
                    member.memberName ??
                    member.name ??
                    "이름 없음"
                ),

            online:
                normalizeBoolean(
                    member.online ??
                    member.isOnline ??
                    member.onlineYn ??
                    false
                ),

            leader:
                normalizeBoolean(
                    member.leader ??
                    member.isLeader ??
                    member.leaderYn ??
                    false
                ),

            role:
                String(
                    member.role ??
                    member.projectRole ??
                    ""
                ),

            profileImage:
                member.profileImage ??
                member.profileImageUrl ??
                member.imageUrl ??
                null
        };
    }


    /* =====================================================
       project.js의 프로젝트 팀원 정보 조회
    ===================================================== */

    function getMembersFromProject(
        projectId
    ) {
        if (
            !app.project ||
            typeof app.project
                .getProjectById !==
            "function"
        ) {
            return [];
        }

        const project =
            app.project.getProjectById(
                projectId
            );

        if (
            !project ||
            !Array.isArray(
                project.members
            )
        ) {
            return [];
        }

        return project.members
            .map(normalizeMember)
            .filter(
                function (member) {
                    return (
                        member.userNum !==
                        null
                    );
                }
            );
    }


    /* =====================================================
       프로젝트별 팀원 저장
    ===================================================== */

    function setProjectMembers(
        projectId,
        members
    ) {
        const normalizedProjectId =
            normalizeNumber(
                projectId
            );

        if (
            normalizedProjectId === null
        ) {
            return;
        }

        const normalizedMembers =
            Array.isArray(members)
                ? members
                    .map(normalizeMember)
                    .filter(
                        function (member) {
                            return (
                                member.userNum !==
                                null
                            );
                        }
                    )
                : [];

        membersByProject.set(
            normalizedProjectId,
            normalizedMembers
        );
    }


    /* =====================================================
       프로젝트 팀원 목록 반환
    ===================================================== */

    function getMembers(projectId) {
        const normalizedProjectId =
            normalizeNumber(
                projectId ??
                app.state
                    ?.currentProjectId
            );

        if (
            normalizedProjectId === null
        ) {
            return [];
        }

        const members =
            membersByProject.get(
                normalizedProjectId
            );

        return Array.isArray(members)
            ? [...members]
            : [];
    }


    /* =====================================================
       userNum으로 팀원 한 명 조회
    ===================================================== */

    function getMemberByUserNum(
        userNum,
        projectId
    ) {
        const normalizedUserNum =
            normalizeNumber(
                userNum
            );

        if (
            normalizedUserNum === null
        ) {
            return null;
        }

        return (
            getMembers(projectId)
                .find(
                    function (member) {
                        return (
                            member.userNum ===
                            normalizedUserNum
                        );
                    }
                ) ??
            null
        );
    }


    /* =====================================================
       팀원 목록 출력

       기존 HTML에 userList가 있을 때만 출력합니다.
       프로젝트 안의 팀원 목록은 project.js가 담당합니다.
    ===================================================== */

    function renderMembers(projectId) {
        const userList =
            document.getElementById(
                "userList"
            );

        if (!userList) {
            return;
        }

        const loginUserNum =
            getLoginUserNum();

        const members =
            getMembers(projectId)
                .filter(
                    function (member) {
                        return (
                            member.userNum !==
                            loginUserNum
                        );
                    }
                );

        userList.innerHTML =
            "";

        if (
            members.length === 0
        ) {
            renderMemberEmpty(
                "개인 채팅이 가능한 팀원이 없습니다."
            );

            return;
        }

        const fragment =
            document.createDocumentFragment();

        members.forEach(
            function (member) {
                fragment.appendChild(
                    createMemberButton(
                        member,
                        projectId
                    )
                );
            }
        );

        userList.appendChild(
            fragment
        );
    }


    /* =====================================================
       개인 채팅 팀원 버튼 생성
    ===================================================== */

    function createMemberButton(
        member,
        projectId
    ) {
        const button =
            document.createElement(
                "button"
            );

        button.type =
            "button";

        button.className =
            "private-member-item";

        button.dataset.action =
            "select-private-member";

        button.dataset.projectId =
            String(projectId);

        button.dataset.userNum =
            String(member.userNum);

        if (
            Number(
                app.state
                    ?.currentPrivateUserNum
            ) === member.userNum
        ) {
            button.classList.add(
                "active"
            );
        }

        const status =
            document.createElement(
                "span"
            );

        status.className =
            member.online
                ? "private-member-status online"
                : "private-member-status offline";

        status.textContent =
            member.online
                ? "🟢"
                : "⚪";

        const name =
            document.createElement(
                "span"
            );

        name.className =
            "private-member-name";

        /*
        화면에는 userNum이나 userId가 아니라
        사용자의 이름을 표시합니다.
        */
        name.textContent =
            member.name;

        button.appendChild(
            status
        );

        button.appendChild(
            name
        );

        if (member.leader) {
            const leader =
                document.createElement(
                    "span"
                );

            leader.className =
                "private-member-leader";

            leader.title =
                "프로젝트 리더";

            leader.textContent =
                "⭐";

            button.appendChild(
                leader
            );
        }

        return button;
    }


    /* =====================================================
       팀원 선택 이벤트 등록
    ===================================================== */

    function bindMemberEvents() {
        const userList =
            document.getElementById(
                "userList"
            );

        if (
            !userList ||
            userList.dataset
                .memberEventBound ===
            "true"
        ) {
            return;
        }

        userList.dataset
            .memberEventBound =
            "true";

        userList.addEventListener(
            "click",
            handleMemberClick
        );

        userList.addEventListener(
            "keydown",
            handleMemberKeydown
        );
    }


    /* =====================================================
       팀원 클릭 처리
    ===================================================== */

    function handleMemberClick(event) {
        const target =
            event.target.closest(
                '[data-action="select-private-member"]'
            );

        if (!target) {
            return;
        }

        const projectId =
            normalizeNumber(
                target.dataset
                    .projectId
            );

        const userNum =
            normalizeNumber(
                target.dataset
                    .userNum
            );

        selectMember(
            userNum,
            projectId
        );
    }


    /* =====================================================
       키보드 팀원 선택
    ===================================================== */

    function handleMemberKeydown(event) {
        if (
            event.key !== "Enter" &&
            event.key !== " "
        ) {
            return;
        }

        const target =
            event.target.closest(
                '[data-action="select-private-member"]'
            );

        if (!target) {
            return;
        }

        event.preventDefault();

        target.click();
    }


    /* =====================================================
       개인 채팅 팀원 선택
    ===================================================== */

    function selectMember(
        userNum,
        projectId
    ) {
        const normalizedProjectId =
            normalizeNumber(
                projectId ??
                app.state
                    ?.currentProjectId
            );

        const normalizedUserNum =
            normalizeNumber(
                userNum
            );

        if (
            normalizedProjectId === null ||
            normalizedUserNum === null
        ) {
            return;
        }

        const loginUserNum =
            getLoginUserNum();

        /*
        자기 자신과는 개인 채팅을 열지 않습니다.
        */
        if (
            normalizedUserNum ===
            loginUserNum
        ) {
            return;
        }

        const project =
            getProject(
                normalizedProjectId
            );

        if (!project) {
            console.warn(
                "팀원을 선택할 프로젝트를 찾을 수 없습니다."
            );

            return;
        }

        let member =
            getMemberByUserNum(
                normalizedUserNum,
                normalizedProjectId
            );

        /*
        member.js 캐시에 없으면 project.js의
        프로젝트 팀원 목록에서 다시 찾습니다.
        */
        if (
            !member &&
            Array.isArray(
                project.members
            )
        ) {
            const rawMember =
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
                );

            if (rawMember) {
                member =
                    normalizeMember(
                        rawMember
                    );
            }
        }

        if (!member) {
            console.warn(
                "선택한 팀원을 찾을 수 없습니다.",
                normalizedUserNum
            );

            return;
        }

        if (
            app.chat &&
            typeof app.chat
                .selectPrivateMember ===
            "function"
        ) {
            app.chat.selectPrivateMember(
                project,
                member
            );

            renderMembers(
                normalizedProjectId
            );

            return;
        }

        /*
        기존 selectUser 함수와의 호환입니다.
        첫 번째 값은 userNum입니다.
        */
        if (
            typeof window.selectUser ===
            "function"
        ) {
            window.selectUser(
                member.userNum,
                member.name
            );
        }
    }


    /* =====================================================
       프로젝트 조회
    ===================================================== */

    function getProject(projectId) {
        if (
            !app.project ||
            typeof app.project
                .getProjectById !==
            "function"
        ) {
            return null;
        }

        return app.project
            .getProjectById(
                projectId
            );
    }


    /* =====================================================
       실시간 접속 상태 변경
    ===================================================== */

    function updateOnlineStatus(
        status
    ) {
        if (!status) {
            return;
        }

        const projectId =
            normalizeNumber(
                status.projectId ??
                app.state
                    ?.currentProjectId
            );

        const userNum =
            normalizeNumber(
                status.userNum ??
                status.memberId ??
                status.id
            );

        if (
            projectId === null ||
            userNum === null
        ) {
            return;
        }

        const members =
            membersByProject.get(
                projectId
            );

        if (!Array.isArray(members)) {
            return;
        }

        const member =
            members.find(
                function (item) {
                    return (
                        item.userNum ===
                        userNum
                    );
                }
            );

        if (!member) {
            return;
        }

        member.online =
            normalizeBoolean(
                status.online ??
                status.isOnline ??
                status.onlineYn
            );

        if (
            Number(
                app.state
                    ?.currentProjectId
            ) === projectId
        ) {
            renderMembers(
                projectId
            );
        }
    }


    /* =====================================================
       현재 프로젝트 팀원 강제 새로고침
    ===================================================== */

    function refreshCurrentMembers() {
        return loadTeamMembers(
            app.state
                ?.currentProjectId,
            true
        );
    }


    /* =====================================================
       로그인 사용자의 userNum 조회
    ===================================================== */

    function getLoginUserNum() {
        const userNum =
            app.state
                ?.loginUser
                ?.userNum ??
            window.currentUserNum ??
            window.currentUserId ??
            null;

        return normalizeNumber(
            userNum
        );
    }


    /* =====================================================
       팀원 로딩 화면
    ===================================================== */

    function renderMemberLoading() {
        const userList =
            document.getElementById(
                "userList"
            );

        if (!userList) {
            return;
        }

        userList.innerHTML = `
            <div class="member-loading">
                팀원 목록을 불러오는 중입니다.
            </div>
        `;
    }


    /* =====================================================
       팀원 빈 화면
    ===================================================== */

    function renderMemberEmpty(
        message
    ) {
        const userList =
            document.getElementById(
                "userList"
            );

        if (!userList) {
            return;
        }

        userList.innerHTML =
            "";

        const empty =
            document.createElement(
                "div"
            );

        empty.className =
            "member-empty";

        empty.textContent =
            message;

        userList.appendChild(
            empty
        );
    }


    /* =====================================================
       팀원 조회 오류 화면
    ===================================================== */

    function renderMemberError(
        projectId
    ) {
        const userList =
            document.getElementById(
                "userList"
            );

        if (!userList) {
            return;
        }

        userList.innerHTML = `
            <div class="member-error">
                <p>
                    팀원 목록을 불러오지 못했습니다.
                </p>

                <button
                    type="button"
                    class="member-reload-button"
                >
                    다시 불러오기
                </button>
            </div>
        `;

        const reloadButton =
            userList.querySelector(
                ".member-reload-button"
            );

        if (reloadButton) {
            reloadButton.addEventListener(
                "click",
                function () {
                    loadTeamMembers(
                        projectId,
                        true
                    );
                }
            );
        }
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
       외부 파일에서 사용할 함수
    ===================================================== */

    app.member = {
        loadTeamMembers,
        refreshCurrentMembers,

        getMembers,
        getMemberByUserNum,
        setProjectMembers,

        renderMembers,
        selectMember,
        updateOnlineStatus
    };


    /* =====================================================
       기존 함수 호출 호환
    ===================================================== */

    window.loadTeamMembers =
        loadTeamMembers;

    window.loadUsers =
        function () {
            renderMembers(
                app.state
                    ?.currentProjectId
            );
        };


    /* =====================================================
       페이지 로드 후 이벤트 등록
    ===================================================== */

    if (
        document.readyState ===
        "loading"
    ) {
        document.addEventListener(
            "DOMContentLoaded",
            bindMemberEvents
        );

    } else {
        bindMemberEvents();
    }

})(
    window.WorkTopusChat =
        window.WorkTopusChat || {}
);