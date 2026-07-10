/* 프로젝트 목록 */

function loadProjects(){

    const projectList =
        document.getElementById("projectList");

    projectList.innerHTML = "";

    const projects = [

        {
            id:1,
            name:"AI Collaboration",
            members:[
                {name:"김여린", online:true, leader:true},
                {name:"신승민", online:true},
                {name:"김경규", online:false},
                {name:"노희진", online:false}
            ]
        },

        {
            id:2,
            name:"쇼핑몰 프로젝트",
            members:[
                {name:"홍길동", online:true},
                {name:"이순신", online:false},
                {name:"유관순", online:true}
            ]
        }
    ];

    projects.forEach(project=>{
        const onlineCount =
            project.members.filter(member => member.online).length;

        projectList.innerHTML += `

    <div class="project-box">

        <!-- 프로젝트 헤더 -->
        <div class="project-header"
             onclick="toggleProject(${project.id})">
                <span id="arrow-${project.id}">
                    ▶
                </span>
    
                <div class="project-info">
                    <div class="project-title">
                        📁 ${project.name}
                    </div>
                    
                    <div class="project-count">
                        👥 ${project.members.length}명
                        &nbsp;&nbsp;
                        🟢 ${onlineCount}명 접속
                    </div>
                </div>
            </div>
    
            <div id="project-${project.id}"
                 class="project-members">
                ${createMemberList(project)}
            </div>
        </div>
                `;
    });
}

/* 팀원 HTML 생성 */
function createMemberList(project){
    let html = "";
    project.members.forEach(member=>{

        html += `
        <div class="member-item"
             onclick="selectProjectMember('${member.name}')">
            ${member.online ? "🟢" : "⚪"}
            ${member.name}
            ${member.leader ? " ⭐" : ""}
        </div>
        `;

    });
    /* 마지막 메시지 */

    html += `
    <div class="last-message">
        <div class="last-message-text">
            💬 김여린 : 회의자료 올렸습니다...
        </div>

        <div class="last-message-time">
            (오후 3:12)
        </div>
    </div>

    <div class="unread-message">
        🔴 안 읽음 3
    </div>
`;
    return html;
}

/* 프로젝트 펼치기 / 접기 */
function toggleProject(projectId){
    const project =
        document.getElementById("project-" + projectId);

    const arrow =
        document.getElementById("arrow-" + projectId);

    if(project.style.maxHeight &&
        project.style.maxHeight !== "0px"){
        project.style.maxHeight="0px";
        arrow.innerHTML="▶";
    }else{
        project.style.maxHeight=
            project.scrollHeight+"px";
        arrow.innerHTML="▼";
    }
}

/* 프로젝트 검색 */

document.addEventListener("input",function(e){
    if(e.target.id!=="projectSearch"){
        return;
    }

    const keyword =
        e.target.value.toLowerCase();

    document
        .querySelectorAll(".project-box")
        .forEach(project=>{

            const title =
                project.querySelector(".project-title")
                    .innerText
                    .toLowerCase();

            if(title.includes(keyword)){
                project.style.display="";
            }else{
                project.style.display="none";
            }
        });
});
