const messageBox = document.querySelector(".msg");

function setMessageText(text) {
    setMessageVisibility(true);
    document.querySelector(".msg").textContent = text;
}

function setMessageVisibility(isVisible) {
    if (!isVisible) {
        messageBox.style.display = "none";
    } else {
        messageBox.style.display = "block"
    }
}

function setMessageTextColor(color) {
    messageBox.style.color = color;
}