console.log("chat.js 로드됨");

let chatMode = "group";
let currentRoom = "group";

function openChat(){
    document.getElementById("chatModal").style.display="block";
    loadProjects();
    loadTeamMembers();
}

function closeChat(){
    document.getElementById("chatModal").style.display="none";
}

function switchTab(type){
    chatMode = type;

    const group =
        document.getElementById("groupChat");

    const privateChat =
        document.getElementById("privateChat");

    const buttons =
        document.querySelectorAll(".chat-tabs button");

    buttons.forEach(btn=>btn.classList.remove("active"));

    if(type==="group"){
        group.style.display="block";
        privateChat.style.display="none";
        buttons[0].classList.add("active");

        currentRoom="group";

        subscribeRoom(currentRoom);
        loadHistory(currentRoom);

    }else{
        group.style.display="none";
        privateChat.style.display="block";
        buttons[1].classList.add("active");
    }
}

function sendMessage(){

    const input =
        document.getElementById("message");

    const text=input.value.trim();
    if(text===""){
        return;
    }

    stompClient.send("/app/chat.send",{},JSON.stringify({
        sender:currentUser,
        message:input.value,
        roomId:currentRoom,
        type:"TALK"
    }));
    input.value="";
}

window.onload = function(){

    console.log("window.onload 실행");
    connect();

    console.log("connect() 호출 완료");
    loadHistory();
    loadProjects();

    document
        .getElementById("sendBtn")
        .addEventListener("click",sendMessage);

    document
        .getElementById("message")
        .addEventListener("keydown",function(e){

            if(e.key==="Enter"){
                sendMessage();
            }
        });
}

/* 프로젝트에서 팀원 선택 */

function selectProjectMember(memberName){

    switchTab("private");
    selectUser(memberName);
}

/* 개인채팅 선택 */

function selectUser(memberName){

    currentRoom=memberName;
    subscribeRoom(currentRoom);
    document.getElementById("privateMessageArea")
        .innerHTML=`
        <h3>👤 ${memberName}</h3>
        <hr>
        <div id="dmArea"></div>
    `;
    loadHistory(currentRoom);
}