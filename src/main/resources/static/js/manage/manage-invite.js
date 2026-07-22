// [manage-invite.js : 팀원 초대 이메일 동적 발송 및 검증 제어]
document.addEventListener("DOMContentLoaded", function () {
    const sendBtn = document.getElementById("sendEmailBtn");
    if (sendBtn) {
        // 중복 바인딩 차단을 위해 기존 속성을 지우고 리스너만 단독 부여합니다.
        sendBtn.removeAttribute("onclick");
        sendBtn.addEventListener("click", sendInviteEmail);
    }
});

function sendInviteEmail() {
    // 입력창 DOM 요소 바인딩
    const emailInput = document.getElementById("emailInput");
    const messageInput = document.getElementById("messageInput");
    const codeInput = document.getElementById("codeInput");

    if (!emailInput || !messageInput || !codeInput) {
        alert("화면 입력 필드를 찾을 수 없습니다.");
        return;
    }

    const emailValue = emailInput.value.trim();
    const messageValue = messageInput.value.trim();
    const codeValue = codeInput.value.trim();

    // 빈 값 예외 처리 및 포커싱 검증
    if (!emailValue) {
        alert("초대할 이메일 주소를 입력해주세요.");
        emailInput.focus();
        return;
    }
    if (!messageValue) {
        alert("보낼 초대 문구를 입력해주세요.");
        messageInput.focus();
        return;
    }
    if (!codeValue) {
        alert("초대 코드를 입력해주세요.");
        codeInput.focus();
        return;
    }

    // 전송용 JSON 객체 팩토리 빌드
    const requestData = {
        email: emailValue,
        message: messageValue,
        code: codeValue
    };

    const sendBtn = document.getElementById("sendEmailBtn");
    if (sendBtn) sendBtn.disabled = true;

    // 시큐리티 규격 CSRF 보안 헤더 파싱
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    const requestHeaders = {
        'Content-Type': 'application/json'
    };

    if (csrfToken && csrfHeader) {
        requestHeaders[csrfHeader] = csrfToken;
    }

    // 백엔드 초대 이메일 발송 API 비동기 통신
    fetch('/api/manage/send-email', {
        method: 'POST',
        headers: requestHeaders,
        body: JSON.stringify(requestData)
    })
        .then(response => response.text())
        .then(data => {
            // 백엔드 문자열 반환 결과에 따른 조건 분기 및 입력폼 리셋
            if (data === "SUCCESS") {
                alert("작성하신 문구와 코드가 포함된 메일이 정상적으로 발송되었습니다!");
                emailInput.value = "";
                messageInput.value = "";
                codeInput.value = "";
            } else if (data === "EMAIL_EMPTY") {
                alert("이메일 주소가 비어있습니다.");
            } else {
                alert("메일 발송에 실패했습니다. 서버 로그를 확인하세요.");
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('네트워크 오류가 발생했습니다.');
        })
        .finally(() => {
            if (sendBtn) sendBtn.disabled = false;
        });
}
