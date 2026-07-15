/**
 *  팀원 초대 (이메일 동적 추가, 형식 검증, 비동기 초대 전송)
 */

/**
 * WorkTopus - 팀원 초대 및 이메일 검증 비동기 통신 가동 엔진
 */
document.addEventListener('DOMContentLoaded', function () {
    const container = document.getElementById('emailInputContainer');
    const addBtn = document.getElementById('addEmailBtn');
    const submitBtn = document.getElementById('submitInviteBtn');

    const existingMembers = ['admin@test.com', 'member@test.com', 'leader@worktopus.com'];
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    // 이메일 입력창 동적 확장
    if (addBtn && container) {
        addBtn.addEventListener('click', function () {
            const newGroup = document.createElement('div');
            newGroup.className = 'email-input-group';

            newGroup.innerHTML = `
                <div class="email-input-flex" style="margin-top: 10px;">
                    <input type="email" name="emails" class="email-main-input" placeholder="초대할 팀원의 이메일을 입력하세요" required>
                    <button type="button" class="email-remove-btn">삭제</button>
                </div>
                <div class="error-msg">올바른 이메일 형식이 아닙니다.</div>
            `;

            newGroup.querySelector('.email-remove-btn').addEventListener('click', function () {
                container.removeChild(newGroup);
            });

            bindValidationEvent(newGroup.querySelector('input[name="emails"]'));
            container.appendChild(newGroup);
        });
    }

    // 입력 형식 실시간 필터 검증
    function bindValidationEvent(inputElement) {
        if (!inputElement) return;
        inputElement.addEventListener('input', function () {
            const value = this.value.trim();
            const group = this.closest('.email-input-group');
            const errorMsg = group.querySelector('.error-msg');

            if (value === '') {
                errorMsg.style.display = 'none';
                return;
            }

            if (!emailRegex.test(value)) {
                errorMsg.innerText = '⚠️ 올바른 이메일 형식이 아닙니다.';
                errorMsg.style.display = 'block';
            } else if (existingMembers.includes(value)) {
                errorMsg.innerText = '⚠️ 이미 워크스페이스에 참여 중인 팀원입니다.';
                errorMsg.style.display = 'block';
            } else {
                errorMsg.style.display = 'none';
            }
        });
    }

    const firstInput = document.querySelector('input[name="emails"]');
    if (firstInput) bindValidationEvent(firstInput);

    // 비동기 초대 REST 통신
    if (submitBtn) {
        submitBtn.addEventListener('click', function (e) {
            e.preventDefault();

            const emailInputs = document.querySelectorAll('input[name="emails"]');
            const emailList = [];
            let hasError = false;

            emailInputs.forEach(input => {
                const value = input.value.trim();
                const group = input.closest('.email-input-group');
                const errorMsg = group.querySelector('.error-msg');

                if (value !== '') {
                    if (!emailRegex.test(value) || existingMembers.includes(value)) {
                        errorMsg.style.display = 'block';
                        hasError = true;
                    } else {
                        emailList.push(value);
                    }
                }
            });

            if (hasError) {
                alert('입력된 이메일 목록 중 올바르지 않은 항목이 존재합니다.');
                return;
            }

            if (emailList.length === 0) {
                alert('최소 하나의 초대 이메일을 입력해야 합니다.');
                return;
            }

            const csrfTokenInput = document.querySelector('input[name="_csrf"]');
            const csrfToken = csrfTokenInput ? csrfTokenInput.value : '';

            const pathSegments = window.location.pathname.split('/');
            const workspaceId = pathSegments[pathSegments.length - 2] || 1;

            const requestData = {
                workspaceId: parseInt(workspaceId),
                emails: emailList
            };

            fetch('/api/manage/invite', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken
                },
                body: JSON.stringify(requestData)
            })
                .then(response => {
                    if (response.ok) return response.json();
                    throw new Error('서버 전송 및 초대 실패');
                })
                .then(data => {
                    alert('선택한 이메일로 팀원 초대가 정상적으로 완료되었습니다.');
                    window.location.href = `/manage/${workspaceId}/members`;
                })
                .catch(error => {
                    console.error('Error details:', error);
                    alert('비동기 전송 실패. 브라우저 캐시 비우기(강력 새로고침) 후 재실행하세요.');
                });
        });
    }
});
