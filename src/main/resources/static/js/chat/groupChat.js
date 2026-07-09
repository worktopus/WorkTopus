function showGroupMessage(message){

    const area = document.getElementById("messageArea");

    area.innerHTML += `
        <div>

            <b>${message.sender}</b>
            : ${message.message}
        </div>
    `;
    area.scrollTop = area.scrollHeight;
}