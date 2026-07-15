const registerForm =
    document.getElementById("registerForm");

const userIdInput =
    document.getElementById("userId");

const checkUserIdButton =
    document.getElementById("checkUserIdButton");

const userIdResult =
    document.getElementById("userIdResult");

let checkedUserId = "";

function showUserIdResult(message, success) {
    userIdResult.textContent = message;

    userIdResult.classList.remove(
        "register-result-success",
        "register-result-error"
    );

    userIdResult.classList.add(
        success
            ? "register-result-success"
            : "register-result-error"
    );
}

checkUserIdButton.addEventListener("click", async function () {
    const userId = userIdInput.value.trim();

    if (userId.length < 4 || userId.length > 30) {
        checkedUserId = "";

        showUserIdResult(
            "아이디는 4~30자로 입력하세요.",
            false
        );

        userIdInput.focus();
        return;
    }

    checkUserIdButton.disabled = true;
    checkUserIdButton.textContent = "확인 중";

    try {
        const response = await fetch(
            `/home/check-id?userId=${encodeURIComponent(userId)}`
        );

        const data = await response.json();

        if (response.ok && data.available === true) {
            checkedUserId = userId;

            showUserIdResult(
                data.message,
                true
            );
        } else {
            checkedUserId = "";

            showUserIdResult(
                data.message,
                false
            );
        }
    } catch (error) {
        checkedUserId = "";

        showUserIdResult(
            "중복확인 중 오류가 발생했습니다.",
            false
        );
    } finally {
        checkUserIdButton.disabled = false;
        checkUserIdButton.textContent = "중복확인";
    }
});

userIdInput.addEventListener("input", function () {
    const currentUserId = userIdInput.value.trim();

    if (checkedUserId && checkedUserId !== currentUserId) {
        checkedUserId = "";

        showUserIdResult(
            "아이디가 변경되었습니다. 다시 중복확인하세요.",
            false
        );
    }

    if (!currentUserId) {
        checkedUserId = "";
        userIdResult.textContent = "";
    }
});

registerForm.addEventListener("submit", function (event) {
    const currentUserId = userIdInput.value.trim();

    if (checkedUserId !== currentUserId) {
        event.preventDefault();

        showUserIdResult(
            "아이디 중복확인을 완료하세요.",
            false
        );

        userIdInput.focus();
    }
});

// 이메일 기능
const emailInput =
    document.getElementById("email");

const sendEmailCodeButton =
    document.getElementById("sendEmailCodeButton");

const emailVerificationCodeInput =
    document.getElementById("emailVerificationCode");

const verifyEmailCodeButton =
    document.getElementById("verifyEmailCodeButton");

const emailVerificationResult =
    document.getElementById("emailVerificationResult");

const csrfToken =
    document.querySelector('meta[name="_csrf"]').content;

const csrfHeader =
    document.querySelector('meta[name="_csrf_header"]').content;

let verifiedEmail = "";

function showEmailResult(message, success) {
    emailVerificationResult.textContent = message;

    emailVerificationResult.classList.remove(
        "register-result-success",
        "register-result-error"
    );

    emailVerificationResult.classList.add(
        success
            ? "register-result-success"
            : "register-result-error"
    );
}

// 인증번호 발송
sendEmailCodeButton.addEventListener(
    "click",
    async function () {
        const email = emailInput.value.trim();

        if (!email) {
            showEmailResult(
                "이메일을 입력해주세요.",
                false
            );

            emailInput.focus();
            return;
        }

        sendEmailCodeButton.disabled = true;
        sendEmailCodeButton.textContent = "발송 중";

        const body = new URLSearchParams({
            email: email
        });

        try {
            const response = await fetch(
                "/home/email/send",
                {
                    method: "POST",
                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded",
                        [csrfHeader]: csrfToken
                    },
                    body: body
                }
            );

            const data = await response.json();

            showEmailResult(
                data.message,
                response.ok
            );

            if (response.ok) {
                verifiedEmail = "";
                emailVerificationCodeInput.value = "";
                emailVerificationCodeInput.focus();
            }

        } catch (error) {
            showEmailResult(
                "인증번호 발송 중 오류가 발생했습니다.",
                false
            );

        } finally {
            sendEmailCodeButton.disabled = false;
            sendEmailCodeButton.textContent =
                "인증번호 발송";
        }
    }
);

// 인증번호 확인
verifyEmailCodeButton.addEventListener(
    "click",
    async function () {
        const email = emailInput.value.trim();

        const verificationCode =
            emailVerificationCodeInput.value.trim();

        if (!verificationCode) {
            showEmailResult(
                "인증번호를 입력해주세요.",
                false
            );

            emailVerificationCodeInput.focus();
            return;
        }

        const body = new URLSearchParams({
            email: email,
            verificationCode: verificationCode
        });

        try {
            const response = await fetch(
                "/home/email/verify",
                {
                    method: "POST",
                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded",
                        [csrfHeader]: csrfToken
                    },
                    body: body
                }
            );

            const data = await response.json();

            showEmailResult(
                data.message,
                response.ok
            );

            verifiedEmail =
                response.ok ? email : "";

            if (response.ok) {
                emailInput.readOnly = true;
                sendEmailCodeButton.disabled = true;
                emailVerificationCodeInput.readOnly = true;
                verifyEmailCodeButton.disabled = true;
            }

        } catch (error) {
            verifiedEmail = "";

            showEmailResult(
                "이메일 인증 중 오류가 발생했습니다.",
                false
            );
        }
    }
);

// 인증 완료 전 이메일을 바꾸면 인증 상태 초기화
emailInput.addEventListener(
    "input",
    function () {
        verifiedEmail = "";
        emailVerificationResult.textContent = "";
    }
);