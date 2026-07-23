document.addEventListener("DOMContentLoaded", function () {

    // 1️⃣ 기존 채팅 버튼 제어 로직 (기존 원본 기능 100% 보존)
    const chatButton = document.getElementById("chatButton");
    if (chatButton) {
        chatButton.addEventListener("click", function () {
            openChat();
        });
    }

    // 2️⃣ 헤더 프로젝트 리스트 동적 드롭다운(스위처) 제어 로직
    const switcherBtn = document.getElementById("projectSwitcherBtn");
    const dropdownList = document.getElementById("projectDropdownList");

    if (switcherBtn && dropdownList) {
        // 프로젝트 화살표 버튼 클릭 시 토글 및 데이터 비동기 로드
        switcherBtn.addEventListener("click", function (e) {
            e.stopPropagation(); // 부모 레이아웃으로의 이벤트 전파 차단하여 레이아웃 꼬임 방지

            // 드롭다운 박스가 닫혀있을 때만 서버에서 데이터를 실시간 조회 후 오픈
            if (dropdownList.style.display === "none" || dropdownList.style.display === "") {

                // 💡 맨 앞에 슬래시(/)를 붙여 절대 경로로 요청하므로 관리자 페이지에서도 주소가 깨지지 않습니다!
                fetch('/api/projects/my-list')
                    .then(res => {
                        if (!res.ok) throw new Error("헤더 프로젝트 목록 서버 응답 오류");
                        return res.json();
                    })
                    .then(data => {
                        if (!data || data.length === 0) {
                            dropdownList.innerHTML = "<div style='padding:10px 16px; color:#94a3b8; font-size:13px; font-family:sans-serif;'>참여 중인 프로젝트가 없습니다.</div>";
                        } else {
                            let html = "<ul style='list-style:none; padding:0; margin:0; font-family:sans-serif;'>";
                            data.forEach(proj => {
                                // 마우스 호버 효과(배경색 변화)를 안전하게 인라인 이벤트로 바인딩
                                html += `<li style='padding:0;'>
                                            <a href="/projects/${proj.id}" 
                                               style='display:block; padding:10px 16px; color:#334155; text-decoration:none; font-size:14px; font-weight:500; transition:background 0.2s;'
                                               onmouseover="this.style.backgroundColor='#f1f5f9'; this.style.color='#4f46e5';" 
                                               onmouseout="this.style.backgroundColor='transparent'; this.style.color='#334155';">
                                                ${proj.name}
                                            </a>
                                         </li>`;
                            });
                            html += "</ul>";
                            dropdownList.innerHTML = html;
                        }
                        dropdownList.style.display = "block"; // 데이터 렌더링이 끝나면 화면에 정상 표시
                    })
                    .catch(err => console.error("❌ 헤더 프로젝트 목록 로드 실패:", err));
            } else {
                dropdownList.style.display = "none"; // 열려있을 때 다시 누르면 닫힘
            }
        });

        // 드롭다운이 열린 상태에서 드롭다운 영역 외 바깥 화면을 클릭하면 자동으로 숨김 처리
        document.addEventListener("click", function (e) {
            if (!switcherBtn.contains(e.target) && !dropdownList.contains(e.target)) {
                dropdownList.style.display = "none";
            }
        });
    }
});
