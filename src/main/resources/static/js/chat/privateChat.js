function createPrivateRoom(myId,targetId){

    const small = Math.min(myId,targetId);
    const big = Math.max(myId,targetId);

    return "private_" + small + "_" + big;
}

function selectUser(memberId,memberName){
    currentRoom = createPrivateRoom(
        currentUserId,
        memberId
    );

    subscribeRoom(currentRoom);

    document.getElementById("privateMessageArea").innerHTML=`

        <h3>${memberName}</h3>
        <hr>
        <div id="dmArea"></div>
    `;

    loadHistory(currentRoom);
}

function showPrivateMessage(message){

    const area = document.getElementById("dmArea");

    if(!area){
        return;
    }

    area.innerHTML += `
        <div>
            <b>${message.sender}</b>
            : ${message.message}
        </div>
    `;
    area.scrollTop = area.scrollHeight;
}