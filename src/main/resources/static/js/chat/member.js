let teamMembers = [];

const currentUserId = 1;      // TODO 로그인 사용자
const currentUser = "신승민";  // TODO 로그인 사용자

async function loadTeamMembers(){

    try{
        const projectId = 1; // TODO 프로젝트 번호
        const response = await fetch("/member/" + projectId);

        teamMembers = await response.json();
        loadUsers();

    }catch(e){
        console.error(e);
    }
}

function loadUsers(){
    const list = document.getElementById("userList");

    list.innerHTML = "";

    teamMembers.forEach(function(member){

        if(member.memberName === currentUser){
            return;
        }

        list.innerHTML += `
            <button
                style="width:100%;padding:10px"
                onclick="selectUser(${member.memberId},'${member.memberName}')">
                👤 ${member.memberName}
            </button>
        `;
    });
}