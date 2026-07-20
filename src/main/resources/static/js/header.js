document.addEventListener("DOMContentLoaded", function () {

    const chatButton = document.getElementById("chatButton");

    if(chatButton){
        chatButton.addEventListener("click", function () {
            openChat();
        });
    }
});