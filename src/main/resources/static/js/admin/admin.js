

const adminUserBtn = document.getElementById("adminUserBtn");
const adminDropdown = document.getElementById("adminDropdown");

adminUserBtn.addEventListener("click", function () {

    adminDropdown.classList.toggle("show");

});

document.addEventListener("click", function (e){

    if(
        !adminUserBtn.contains(e.target) &&
        !adminDropdown.contains(e.target)
    ){

        adminDropdown.classList.remove("show");

    }

});
