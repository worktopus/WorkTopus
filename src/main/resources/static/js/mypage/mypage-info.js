// 프사 미리보기
function previewImage(input) {
    if (input.files && input.files[0]) {
        var reader = new FileReader();
        reader.onload = function(e) {
            var label = document.querySelector('.profile-avatar-label');
            label.innerHTML = `<img src="${e.target.result}" id="profile-preview" class="profile-click-avatar" title="사진을 클릭하여 변경">`;
        }
        reader.readAsDataURL(input.files[0]);
    }
}