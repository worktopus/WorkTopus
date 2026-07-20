// 투두리스트 모달 제어, API 비동기 처리
function toggleTodoModal(event) {
    event.stopPropagation();
    const modal = document.getElementById('todo-popup-modal');
    const isActive = modal.classList.contains('active');

    if (isActive) {
        if (typeof forceSaveCurrentMemo === 'function') forceSaveCurrentMemo();
        modal.classList.remove('active');
    } else {
        // 메모를 새로 여는 시점: 프로필 드롭다운이 켜져 있다면 닫기
        const profileDropdown = document.getElementById('profile-dropdown-menu');
        if (profileDropdown) profileDropdown.classList.remove('active');

        modal.classList.add('active');
        setTimeout(() => {
            if (typeof loadMemo === 'function') loadMemo();
        }, 0);
    }
}

// 팝업 내부 클릭 시 바깥 클릭 이벤트 전파 차단
// (DOMContentLoaded 바깥에 선언하거나, 함수 내부 혹은 파일 로드 시점에 바로 등록)
document.addEventListener('DOMContentLoaded', () => {
    const todoModal = document.getElementById('todo-popup-modal');
    if (todoModal) {
        todoModal.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    }
});

// 투두리스트 / 메모장 비동기 처리 스크립트
let autoSaveTimeout = null;

// 1. 메모 불러오기 (DOM 매핑 보강)
function loadMemo() {
    const statusText = document.getElementById('memo-save-status');
    const textarea = document.getElementById('popup-memo-textarea');

    // 엘리먼트가 존재하지 않으면 강제 종료
    if (!textarea) return;
    if (statusText) statusText.innerText = "불러오는 중...";

    fetch("/api/todo")
        .then(res => {
            if (!res.ok) throw new Error("네트워크 응답 불량");
            return res.json();
        })
        .then(list => {
            if (list && list.length > 0) {
                // 가장 최신 데이터의 내용을 입력창에 매핑
                textarea.value = list[0].content;
                textarea.setAttribute('data-id', list[0].todoId);
            } else {
                textarea.value = '';
                textarea.removeAttribute('data-id');
            }
            if (statusText) statusText.innerText = "저장됨";
        })
        .catch(err => {
            console.error("메모 로드 실패:", err);
            if (statusText) statusText.innerText = "불러오기 실패";
        });
}

// 2. 메모 저장하기 (추가 혹은 수정)
function saveMemo() {
    const textarea = document.getElementById('popup-memo-textarea');
    if (!textarea) return;

    const text = textarea.value.trim(); // 공백 제거
    const todoId = textarea.getAttribute('data-id');
    const statusText = document.getElementById('memo-save-status');

    const requestHeaders = {
        "Content-Type": "application/json"
    };
    if (typeof csrfHeader !== 'undefined' && typeof csrfToken !== 'undefined' && csrfHeader && csrfToken) {
        requestHeaders[csrfHeader] = csrfToken;
    }

    // HTML에 선언된 CSRF 변수 사용
    if (typeof csrfHeader !== 'undefined' && typeof csrfToken !== 'undefined' && csrfHeader && csrfToken) {
        requestHeaders[csrfHeader] = csrfToken;
    }

    // 텍스트가 비어있는데 기존에 저장된 ID가 있다면 -> 삭제 프로세스 진행
    if (text === "") {
        if (todoId) {
            if (statusText) statusText.innerText = "삭제 중...";
            fetch(`/api/todo/${todoId}`, {
                method: "DELETE",
                headers: requestHeaders
            })
                .then(res => {
                    if (res.ok) {
                        textarea.removeAttribute('data-id'); // ID 제거
                        if (statusText) statusText.innerText = "저장됨(비어있음)";
                    } else {
                        if (statusText) statusText.innerText = "삭제 실패";
                    }
                })
                .catch(err => {
                    console.error("메모 삭제 실패:", err);
                    if (statusText) statusText.innerText = "삭제 오류";
                });
        }
        return; // 빈 값이면 이후 저장 로직 진행 안 함
    }

    if (todoId) {
        // 기존 데이터 수정 (PATCH)
        fetch(`/api/todo/${todoId}`, {
            method: "PATCH",
            headers: requestHeaders,
            body: JSON.stringify({ content: text })
        })
            .then(res => {
                if (res.ok) {
                    if (statusText) statusText.innerText = "저장됨";
                } else {
                    if (statusText) statusText.innerText = "저장 실패";
                }
            })
            .catch(err => {
                console.error("메모 수정 실패:", err);
                if (statusText) statusText.innerText = "저장 오류";
            });
    } else {
        // 신규 메모 생성 (POST)
        fetch("/api/todo", {
            method: "POST",
            headers: requestHeaders,
            body: JSON.stringify({ content: text })
        })
            .then(res => {
                if (res.ok) {
                    return res.json();
                }
                throw new Error("신규 저장 실패");
            })
            .then(data => {
                // 새로 생성된 ID 매핑을 위해 재로드하지 않고 직접 세팅하여 딜레이 방지
                if (data && data.todoId) {
                    textarea.setAttribute('data-id', data.todoId);
                }
                if (statusText) statusText.innerText = "저장됨";
            })
            .catch(err => {
                console.error("메모 추가 실패:", err);
                if (statusText) statusText.innerText = "저장 오류";
            });
    }
}

// 3. 디바운싱 기반 자동 저장 트리거
function triggerAutoSave() {
    const statusText = document.getElementById('memo-save-status');
    if (statusText) statusText.innerText = "입력 중...";

    if (autoSaveTimeout) {
        clearTimeout(autoSaveTimeout);
    }

    autoSaveTimeout = setTimeout(() => {
        saveMemo();
    }, 1000);
}

// 4. 창을 완전히 닫거나 흐름이 끊길 때 조건 없이 즉시 저장시키는 헬퍼 함수
function forceSaveCurrentMemo() {
    // 대기 중인 타이머가 있다면 일단 클리어 시켜서 중복 호출 방지
    if (autoSaveTimeout) {
        clearTimeout(autoSaveTimeout);
        autoSaveTimeout = null;
    }
    // 타이머 조건문(if)을 제거하고 무조건 최종 자취를 남기도록 saveMemo() 직접 실행
    saveMemo();
}