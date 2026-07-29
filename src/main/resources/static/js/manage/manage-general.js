document.addEventListener("DOMContentLoaded", function () {

    // 1. 현재 URL 주소창에서 정규식을 이용해 프로젝트 고유 숫자 ID만 완벽하게 추출
    const currentPath = window.location.pathname; // 예: "/projects/manage/28"
    const match = currentPath.match(/\/manage\/(\d+)/);

    if (!match || !match[1]) {
        console.error("❌ [오류] URL에서 프로젝트 고유 ID를 파싱할 수 없습니다.");
        return;
    }

    // 📌 [교정 완료] match 객체가 아니라 match[1] 배열 요소를 정확히 꺼내야 "28"이 담깁니다.
    const workspaceId = match[1];
    console.log("▶ [안전 추출된 워크스페이스 ID 확인] :", workspaceId);

    // Spring Security CSRF 검증 통과를 위한 메타 태그 실시간 수집
    const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    const csrfToken = csrfTokenMeta ? csrfTokenMeta.getAttribute("content") : "";
    const csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.getAttribute("content") : "";

    const nameForm = document.getElementById("updateNameForm");
    const descForm = document.getElementById("updateDescForm");


    // 삭제 관련 요소
    const openDeleteModalBtn =
        document.getElementById("openDeleteProjectModalBtn");

    const deleteProjectModal =
        document.getElementById("deleteProjectModal");

    const closeDeleteModalBtn =
        document.getElementById("closeDeleteProjectModalBtn");

    const deleteProjectNameInput =
        document.getElementById("deleteProjectNameInput");

    const currentProjectNameInput =
        document.getElementById("currentProjectName");

    const confirmDeleteProjectBtn =
        document.getElementById("confirmDeleteProjectBtn");

    const deleteProjectError =
        document.getElementById("deleteProjectError");

    // 2. 📌 프로젝트 이름 비동기 수정 처리 구역
    if (nameForm) {
        nameForm.addEventListener("submit", function (e) {
            e.preventDefault();

            const formData = new FormData(nameForm);

            // 주소 맨 앞에 슬래시(/)를 명시하여 절대 경로 형태로 타겟 API 엔드포인트 강제 지정
            fetch("/api/manage/" + workspaceId + "/update-name", {
                method: "POST",
                headers: {
                    [csrfHeader]: csrfToken
                },
                body: formData
            })
                .then(res => {
                    if (!res.ok) throw new Error("서버 404/400 응답 실패 - 주소 오타 혹은 매핑 누락");
                    return res.json();
                })
                .then(data => {
                    if (data.message) {
                        alert("프로젝트 이름이 성공적으로 변경되었습니다. 🎉");

                        // 우측 상단 헤더 텍스트 실시간 반영 변경
                        const changedProjectName =
                            document.getElementById("projectName").value.trim();

                        const headerProjName =
                            document.querySelector(".header__project-name");

                        if (headerProjName) {
                            headerProjName.textContent = changedProjectName;
                        }

                        if (currentProjectNameInput) {
                            currentProjectNameInput.value = changedProjectName;
                        }
                    } else if (data.error) {
                        alert("수정 실패: " + data.error);
                    }
                })
                .catch(err => {
                    console.error("이름 변경 처리 중 에러 발생:", err);
                    alert("설정 저장 중 오류가 발생했습니다. 브라우저 콘솔 확인 필요");
                });
        });
    }

    // 3. 📌 프로젝트 내용(설명) 비동기 수정 처리 구역
    if (descForm) {
        descForm.addEventListener("submit", function (e) {
            e.preventDefault();

            const formData = new FormData(descForm);

            // 주소 맨 앞에 슬래시(/)를 명시하여 절대 경로 형태로 타겟 API 엔드포인트 강제 지정
            fetch("/api/manage/" + workspaceId + "/update-description", {
                method: "POST",
                headers: {
                    [csrfHeader]: csrfToken
                },
                body: formData
            })
                .then(res => {
                    if (!res.ok) throw new Error("서버 응답 실패");
                    return res.json();
                })
                .then(data => {
                    if (data.message) {
                        alert("프로젝트 내용이 성공적으로 변경되었습니다. 🎉");
                    } else if (data.error) {
                        alert("수정 실패: " + data.error);
                    }
                })
                .catch(err => {
                    console.error("내용 변경 처리 중 에러 발생:", err);
                    alert("설정 저장 중 오류가 발생했습니다. 브라우저 콘솔 확인 필요");
                });
        });
    }

    // 프로젝트 삭제 모달 열기
    if (
        openDeleteModalBtn &&
        deleteProjectModal &&
        deleteProjectNameInput &&
        confirmDeleteProjectBtn
    ) {
        openDeleteModalBtn.addEventListener("click", function () {
            deleteProjectModal.hidden = false;
            deleteProjectNameInput.value = "";
            confirmDeleteProjectBtn.disabled = true;
            confirmDeleteProjectBtn.textContent = "영구 삭제";

            if (deleteProjectError) {
                deleteProjectError.textContent = "";
                deleteProjectError.hidden = true;
            }

            deleteProjectNameInput.focus();
        });
    }


// 프로젝트 삭제 모달 닫기
    if (closeDeleteModalBtn && deleteProjectModal) {
        closeDeleteModalBtn.addEventListener("click", function () {
            closeDeleteProjectModal();
        });
    }


// 모달 배경 클릭 시 닫기
    if (deleteProjectModal) {
        deleteProjectModal.addEventListener("click", function (event) {
            if (event.target === deleteProjectModal) {
                closeDeleteProjectModal();
            }
        });
    }


// 입력한 프로젝트명 검증
    if (
        deleteProjectNameInput &&
        currentProjectNameInput &&
        confirmDeleteProjectBtn
    ) {
        deleteProjectNameInput.addEventListener("input", function () {
            const enteredName = deleteProjectNameInput.value.trim();
            const currentName = currentProjectNameInput.value.trim();

            confirmDeleteProjectBtn.disabled =
                enteredName !== currentName;

            if (deleteProjectError) {
                deleteProjectError.textContent = "";
                deleteProjectError.hidden = true;
            }
        });
    }


// 프로젝트 영구 삭제
    if (
        confirmDeleteProjectBtn &&
        deleteProjectNameInput &&
        currentProjectNameInput
    ) {
        confirmDeleteProjectBtn.addEventListener("click", async function () {
            const enteredName = deleteProjectNameInput.value.trim();
            const currentName = currentProjectNameInput.value.trim();

            if (enteredName !== currentName) {
                showDeleteError("프로젝트 이름이 일치하지 않습니다.");
                return;
            }

            confirmDeleteProjectBtn.disabled = true;
            confirmDeleteProjectBtn.textContent = "삭제 중...";

            try {
                const response = await fetch(
                    `/api/manage/${workspaceId}`,
                    {
                        method: "DELETE",
                        headers: {
                            [csrfHeader]: csrfToken
                        }
                    }
                );

                const data = await response.json();

                if (!response.ok) {
                    throw new Error(
                        data.error || "프로젝트 삭제에 실패했습니다."
                    );
                }

                window.location.href =
                    data.redirectUrl || "/projects";

            } catch (error) {
                console.error("프로젝트 삭제 오류:", error);

                showDeleteError(error.message);

                confirmDeleteProjectBtn.disabled = false;
                confirmDeleteProjectBtn.textContent = "영구 삭제";
            }
        });
    }


    function showDeleteError(message) {
        if (!deleteProjectError) {
            return;
        }

        deleteProjectError.textContent = message;
        deleteProjectError.hidden = false;
    }


    function closeDeleteProjectModal() {
        if (!deleteProjectModal) {
            return;
        }

        deleteProjectModal.hidden = true;

        if (deleteProjectNameInput) {
            deleteProjectNameInput.value = "";
        }

        if (confirmDeleteProjectBtn) {
            confirmDeleteProjectBtn.disabled = true;
            confirmDeleteProjectBtn.textContent = "영구 삭제";
        }

        if (deleteProjectError) {
            deleteProjectError.textContent = "";
            deleteProjectError.hidden = true;
        }
    }

});
