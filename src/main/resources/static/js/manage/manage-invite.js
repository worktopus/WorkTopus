/**
 * WorkTopus 프로젝트 - 팀원 초대 이메일 발송 스크립트 (동적 데이터 반영)
 */
document.addEventListener("DOMContentLoaded", function () {
    const sendBtn = document.getElementById("sendEmailBtn");
    if (sendBtn) {
        sendBtn.addEventListener("click", sendInviteEmail);
    }
});

function sendInviteEmail() {
    // HTML에 생성한 3가지 입력창 요소를 가져옵니다.
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

    // 입력값 기본 검증
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

    // [변경사항] 이제 고정값이 아니라 사용자가 화면에 입력한 값을 JSON으로 세팅합니다.
    const requestData = {
        email: emailValue,
        message: messageValue,
        code: codeValue
    };

    const sendBtn = document.getElementById("sendEmailBtn");
    if (sendBtn) sendBtn.disabled = true;

    // Security CSRF 처리
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    const requestHeaders = {
        'Content-Type': 'application/json'
    };

    if (csrfToken && csrfHeader) {
        requestHeaders[csrfHeader] = csrfToken;
    }

    // 백엔드로 발송 요청
    fetch('/api/manage/send-email', {
        method: 'POST',
        headers: requestHeaders,
        body: JSON.stringify(requestData)
    })
        .then(response => response.text())
        .then(data => {
            if (data === "SUCCESS") {
                alert("작성하신 문구와 코드가 포함된 메일이 정상적으로 발송되었습니다!");
                // 입력창 초기화
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
