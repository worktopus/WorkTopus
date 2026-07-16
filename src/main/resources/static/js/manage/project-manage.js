/**
 *  모든 탭에서 공통으로 쓰는 코드 (예: 상단 탭 전환 자바스크립트 등)
 * WorkTopus - 관리 대시보드 및 팀원 초대/일반 설정 비동기 통신 가동 엔진 (시큐리티 CSRF 격파 버전)
 */
/**
 * WorkTopus - 관리 대시보드 공통 및 UI 제어 엔진
 */
document.addEventListener('DOMContentLoaded', function () {

    // 1. 상단 탭 메뉴 전환 기능 제어
    const tabButtons = document.querySelectorAll('.manage-tabs .tab-btn');

    tabButtons.forEach(button => {
        button.addEventListener('click', function () {
            // 클릭한 버튼의 텍스트 콘텐츠를 기반으로 타겟 탭 ID를 유연하게 추출합니다.
            let tabId = '';

            if (this.textContent.includes('일반')) {
                tabId = 'general-tab';
            } else if (this.textContent.includes('팀원')) {
                // HTML의 "팀원 관리"와 "팀원 초대" 버튼 텍스트를 정밀 분기 처리합니다.
                if (this.textContent.includes('초대')) {
                    tabId = 'invite-tab';
                } else {
                    tabId = 'member-tab';
                }
            } else if (this.textContent.includes('게시판')) {
                tabId = 'board-tab';
            }

            // 모든 탭 콘텐츠 영역을 보이지 않도록 숨김 처리합니다.
            const contents = document.querySelectorAll('.tab-content');
            contents.forEach(content => {
                content.classList.remove('active');
                content.style.display = 'none';
            });

            // 모든 탭 버튼의 활성화 하이라이트 클래스를 일시적으로 전부 제거합니다.
            tabButtons.forEach(btn => btn.classList.remove('active'));

            // 추적된 ID에 해당하는 탭 화면만 정확하게 활성화 및 노출(Block)시킵니다.
            const targetContent = document.getElementById(tabId);
            if (targetContent) {
                targetContent.classList.add('active');
                targetContent.style.display = 'block';
            } else {
                console.warn(`[탭 전환 경고] 매핑 대상 탭 구역 ID가 존재하지 않습니다: ${tabId}`);
            }

            // 현재 클릭된 버튼에 활성화 스타일을 부여합니다.
            this.classList.add('active');
        });
    });

    // 2. 단독 링크 클립보드 복사 기능
    const copyLinkBtn = document.getElementById('copyLinkBtn');
    if (copyLinkBtn) {
        copyLinkBtn.addEventListener('click', function () {
            const linkInput = document.getElementById('inviteLinkInput');
            if (linkInput) {
                linkInput.select();
                navigator.clipboard.writeText(linkInput.value)
                    .then(() => alert('🔗 초대 링크가 클립보드에 복사되었습니다.'))
                    .catch(err => console.error('복사 에러:', err));
            }
        });
    }
});
