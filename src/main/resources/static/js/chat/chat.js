let chatMode = "group";
let currentRoom = "group";

function openChat(){
    document.getElementById("chatModal").style.display="block";
    loadTeamMembers();
}

function closeChat(){
    document.getElementById("chatModal").style.display="none";
}

function switchTab(type){
    chatMode = type;
    const group =  document.getElementById("groupChat");
    const privateChat =  document.getElementById("privateChat");

    const buttons =  document.querySelectorAll(".tab button");

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

    if(input.value.trim()===""){
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

window.onload=function(){

    connect();
    loadHistory("group");
    document.getElementById("sendBtn")
        .addEventListener("click",sendMessage);

    document.getElementById("message")
        .addEventListener("keydown",function(e){

            if(e.key==="Enter"){
                sendMessage();
            }
        });
};