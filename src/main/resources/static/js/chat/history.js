async function loadHistory(roomId){

    const response =
        await fetch("/chat/history/" + roomId);

    const list =
        await response.json();

    if(roomId === "group"){
        document.getElementById("messageArea").innerHTML="";
        list.forEach(showGroupMessage);
    }else{
        const area =
            document.getElementById("dmArea");
        if(area){
            area.innerHTML="";
            list.forEach(showPrivateMessage);
        }
    }
}