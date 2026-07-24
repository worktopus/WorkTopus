// 1. 이미지 선택 시 미리보기 / 초기화 플래그(isDefaultProfile) 해제
function previewImage(input) {
    if (input.files && input.files[0]) {
        var reader = new FileReader();
        reader.onload = function(e) {
            var label = document.querySelector('.profile-avatar-label');
            label.innerHTML = `<img src="${e.target.result}" id="profile-preview" class="profile-click-avatar" title="사진을 클릭하여 변경">`;
        }
        reader.readAsDataURL(input.files[0]);

        // 파일이 선택되었으므로 초기화 플래그를 false로 설정
        var isDefaultInput = document.getElementById('isDefaultProfile');
        if (isDefaultInput) isDefaultInput.value = "false";
    }
}

// 2.  기본 이미지로 초기화 버튼 클릭 시 실행
function resetToDefaultProfile() {
    var label = document.querySelector('.profile-avatar-label');
    // 기본 이미지 경로(/images/logo.png)로 미리보기 변경
    label.innerHTML = `<img src="/images/logo.png" id="profile-preview" class="profile-click-avatar" title="사진을 클릭하여 변경">`;

    // file input 초기화
    var fileInput = document.getElementById('profile-file-input');
    if (fileInput) fileInput.value = "";

    // 서버로 보낼 hidden input 값을 true로 설정
    var isDefaultInput = document.getElementById('isDefaultProfile');
    if (isDefaultInput) isDefaultInput.value = "true";
}