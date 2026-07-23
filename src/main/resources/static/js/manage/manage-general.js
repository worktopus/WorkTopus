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
                        const headerProjName = document.querySelector(".header__project-name");
                        if (headerProjName) {
                            headerProjName.textContent = document.getElementById("projectName").value;
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
});
