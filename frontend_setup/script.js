function showSignup() {
    document.querySelector(".login").style.opacity = "0";
    document.querySelector(".login").style.pointerEvents = "none";

    document.querySelector(".signup").style.opacity = "1";
    document.querySelector(".signup").style.pointerEvents = "auto";
}

function showLogin() {
    document.querySelector(".signup").style.opacity = "0";
    document.querySelector(".signup").style.pointerEvents = "none";

    document.querySelector(".login").style.opacity = "1";
    document.querySelector(".login").style.pointerEvents = "auto";
}
