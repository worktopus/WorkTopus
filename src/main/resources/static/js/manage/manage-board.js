// [manage-board.js] 1단계 : 오라클 실시간 연동 및 동적 행(tr) 조립 (버튼 주석 제어 구역)
document.addEventListener('DOMContentLoaded', function () {
    const postContainer = document.getElementById('integratedPostTbody');
    const chipContainer = document.getElementById('memberFilterChips');

    // 📊 주소창 /projects/43/manage 포맷에서 프로젝트 ID(43)를 정확히 정제합니다.
    const uri = window.location.pathname;
    const segments = uri.split('/').filter(Boolean);

    let projectId = null;
    const projectsIndex = segments.indexOf('projects');
    if (projectsIndex !== -1 && projectsIndex + 1 < segments.length) {
        projectId = segments[projectsIndex + 1];
    } else {
        projectId = chipContainer?.getAttribute('data-project-id') || '43';
    }

    // CSRF 시큐리티 보안 토큰 수집
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

    const getHeaders = () => {
        const headers = { 'Content-Type': 'application/json' };
        if (csrfToken) headers[csrfHeader] = csrfToken;
        return headers;
    };

    // 전역 메모리 버퍼 버킷 선언
    let globalCachedBoards = [];

    // 📊 [오라클 실시간 통계 연동]
    if (projectId) {
        fetch(`/api/projects/${projectId}/manage/board-stats`)
            .then(res => res.json())
            .then(data => {
                if (data && data.totalPosts !== undefined) {
                    document.getElementById('totalPostsCount').textContent = data.totalPosts;
                }
            })
            .catch(err => console.error("오라클 통계 데이터 수집 실패:", err));
    }

    // 👥 팀원 명단 수집 후 상단 필터 칩 버튼 동적 생성 가동
    if (projectId) {
        loadProjectFilterChips();
    }

    function loadProjectFilterChips() {
        if (!chipContainer) return;

        fetch(`/api/projects/${projectId}/manage/members-data`)
            .then(res => res.json())
            .then(members => {
                chipContainer.innerHTML = '';

                // 1. [전체보기] 기본 마스터 활성화 칩 배정
                const allBtn = document.createElement('button');
                allBtn.type = 'button';
                allBtn.className = 'filter-chip-btn active';
                allBtn.textContent = '✨ 전체 게시글 보기';
                allBtn.setAttribute('data-target-writer', 'ALL');
                chipContainer.appendChild(allBtn);

                // 2. 참여 구성원 명단 순회 루프 작동 (NAME 문자열 매칭 규칙)
                members.forEach(member => {
                    const btn = document.createElement('button');
                    btn.type = 'button';
                    btn.className = 'filter-chip-btn';

                    const userRealName = member.userName || member.name || '알 수 없음';

                    btn.textContent = `👤 ${userRealName}`;
                    btn.setAttribute('data-target-writer', userRealName);
                    chipContainer.appendChild(btn);
                });

                // 3. 클릭 실시간 칩 고속 필터링 바인딩 엔진 호출
                bindChipClickEvents();
            })
            .catch(err => console.error("오라클 팀원 명단 칩 생성 실패:", err));
    }

    // 📝 [오라클 연동] 프로젝트 내부 통합 게시글 원본 데이터 일괄 호출
    function loadInitialProjectPosts() {
        if (!postContainer) return;
        postContainer.innerHTML = '<tr><td colspan="3">오라클 DB에서 프로젝트 통합 게시글 수집 중...</td></tr>';

        fetch(`/api/projects/${projectId}/manage/board-contents`)
            .then(res => {
                if (!res.ok) throw new Error("통합 데이터 수집 실패");
                return res.json();
            })
            .then(listData => {
                globalCachedBoards = listData;
                renderIntegratedTable(globalCachedBoards);
            })
            .catch(err => {
                console.error(err);
                postContainer.innerHTML = '<tr><td colspan="3">오라클 실시간 데이터 통합 연동 중 에러가 발생했습니다.</td></tr>';
            });
    }

    // 📊 [최종 출력 렌더링 엔진] 카테고리 명칭 한글 배지 결합 구조 사출
    function renderIntegratedTable(boards) {
        postContainer.innerHTML = '';

        if (!Array.isArray(boards) || boards.length === 0) {
            postContainer.innerHTML = '<tr><td colspan="3">해당 팀원이 작성한 게시글 데이터가 존재하지 않습니다.</td></tr>';
            return;
        }

        boards.forEach(board => {
            const tr = document.createElement('tr');
            tr.className = 'board-item-row';
            tr.setAttribute('data-post-id', board.id);

            const noticeTag = board.notice ? '<span>[공지] </span>' : '';
            const contentExcerpt = board.contentPreview ? board.contentPreview : '본문 내용 없음';
            const formattedDate = board.createdAt ? String(board.createdAt).slice(0, 10).replaceAll('-', '.') : '-';
            const writerDisplay = board.writerName || '익명';

            // 카테고리 한글 변환 분기 처리
            let categoryKorean = '기타';
            if (board.category) {
                const catUpper = board.category.toUpperCase();
                switch (catUpper) {
                    case 'NOTICE': categoryKorean = '공지'; break;
                    case 'MEETING': categoryKorean = '회의'; break;
                    case 'WORK': categoryKorean = '업무'; break;
                    case 'RESOURCE': categoryKorean = '자료'; break;
                    case 'IDEA': categoryKorean = '아이디어'; break;
                    case 'ETC': categoryKorean = '기타'; break;
                    default: categoryKorean = board.category; break;
                }
            }

            let categoryBadgeHtml = '';
            if (board.category) {
                categoryBadgeHtml = `<span class="badge-cat badge-${board.category.toLowerCase()}">${categoryKorean}</span> `;
            }

            // 💡 [🚨 수정 / 삭제 버튼 최종 봉인 주석화 완료]
            tr.innerHTML = `
                <td>
                    <div class="member-name-text">${noticeTag}${categoryBadgeHtml}${board.title || '제목 없음'}</div>
                    <div class="member-email-text">${contentExcerpt}</div>
                </td>
                <td>
                    <div class="member-name-text">${writerDisplay}</div>
                    <div class="member-email-text">${formattedDate} (👀 ${board.viewCount ?? 0}회)</div>
                </td>
                <td>
                    <!-- 🚫 요구사항에 맞춰 테이블 목록 내 제어 버튼 구역을 전면 주석화 및 블라인드 처리했습니다 -->
                    <!-- <button type="button" class="btn btn-edit-post" data-id="${board.id}">수정</button> -->
                    <!-- <button type="button" class="btn btn-delete-post" data-id="${board.id}">삭제</button> -->
                    <span style="color: var(--text-sub, #718096); font-size: 0.85rem;">조회 전용 모드</span>
                </td>
            `;
            postContainer.appendChild(tr);
        });
    }

    if (projectId) {
        loadInitialProjectPosts();
    }
    // 👥 오라클 USERS.NAME - PROJECT_BOARD.WRITER_NAME 문자열 매칭 필터 제어 엔진 (가로 드래그 스크롤 스왑 유지)
    function bindChipClickEvents() {
        if (!chipContainer) return;

        chipContainer.addEventListener('click', function(e) {
            const targetBtn = e.target.closest('.filter-chip-btn');
            if (!targetBtn) return;

            // 1. 기존에 선택되어 있던 칩의 활성화 클래스(active)를 걷어내고 현재 클릭한 칩에 주입
            chipContainer.querySelectorAll('.filter-chip-btn').forEach(btn => {
                btn.classList.remove('active');
            });
            targetBtn.classList.add('active');

            // 2. 칩에 바인딩된 대상 팀원의 진짜 이름(NAME) 문자열 추출
            const targetWriterName = targetBtn.getAttribute('data-target-writer');

            // 3. 'ALL'이면 전체 데이터 출력, 특정 팀원이면 WRITER_NAME 컬럼 데이터와 글자 그대로 완전 일치(===) 필터링
            if (targetWriterName === 'ALL') {
                renderIntegratedTable(globalCachedBoards);
            } else {
                const filteredBoards = globalCachedBoards.filter(board => {
                    return board.writerName === targetWriterName;
                });
                renderIntegratedTable(filteredBoards);
            }
        });
    }

    // 📌 통합 게시글 목록 내 수정 및 삭제 클릭 트리거 이벤트 안전 봉인 구역
    if (postContainer) {
        /* 🚫 [버튼 주석화 조치] 수정 버튼 클릭 시 정식 팀장 전용 상세 제어창 이동 로직을 주석 처리하여 봉인합니다.
        postContainer.addEventListener('click', function (e) {
            if (e.target && e.target.classList.contains('btn-edit-post')) {
                const targetBtn = e.target;
                const postId = targetBtn.getAttribute('data-id');
                window.location.href = `/projects/manage/${projectId}/boards/${postId}`;
            }
        });
        */

        /* 🚫 [버튼 주석화 조치] 삭제 버튼 클릭 시 격리 삭제 보관함 팝업 모달 트리거 로직을 주석 처리하여 봉인합니다.
        postContainer.addEventListener('click', function (e) {
            if (e.target && e.target.classList.contains('btn-delete-post')) {
                const targetBtn = e.target;
                const postId = targetBtn.getAttribute('data-id');
                const row = targetBtn.closest('tr');

                const currentTitle = row.querySelector('.member-name-text').textContent
                    .replace('[공지]', '')
                    .replace(row.querySelector('.badge-cat')?.textContent || '', '')
                    .trim();

                const deleteModal = document.getElementById('deleteBoardModal');

                document.getElementById('deleteTargetBoardId').value = postId;
                document.getElementById('deleteTargetBoardName').textContent = currentTitle;
                deleteModal.style.display = 'flex';
            }
        });
        */
    }

    // 🚫 [모달 폐쇄 조치] 프론트엔드 모달 레이어 수립부도 깔끔하게 작동 방지 처리합니다.
    function injectManagementModals(csrfToken, csrfHeader, getHeaders) {
        // 더 이상 화면 하단에 팝업 요소를 동적 주입하지 않고 자원을 반환합니다.
        System.out.println("▶ [보안 제어] 관리자 팝업 모달 마운트가 차단되었습니다.");
    }
});
