let stompClient = null;
let subscription = null;

function connect() {

    const socket = new SockJS('/chat');

    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {
        console.log("WebSocket 연결 성공");

        subscribeRoom(currentRoom);
    });
}

function subscribeRoom(roomId){

    if(subscription){
        subscription.unsubscribe();
    }

    subscription = stompClient.subscribe(
        "/topic/chat/" + roomId,
        function(message){

            const msg = JSON.parse(message.body);

            if(roomId === "group"){
                showGroupMessage(msg);
            }else{
                showPrivateMessage(msg);
            }

        }
    );

}