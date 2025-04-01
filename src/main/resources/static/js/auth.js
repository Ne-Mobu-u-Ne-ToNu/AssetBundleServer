async function checkAuth() {
    try {
        const response = await fetch('/api/secured/username', {
            method: 'GET',
            credentials: 'include'
        });

        if (response.ok) {
            const data = await response.json();

            document.getElementById("auth-link").textContent = data.username;
            document.getElementById("authorizationRef").style.display = "none";
            document.getElementById("profileRef").style.display = "block";
            document.getElementById("myBundlesRef").style.display = "block";
            document.getElementById("uploadFileRef").style.display = "block";
            document.getElementById("logoutRef").style.display = "block";
        } else {
            document.getElementById("auth-link").textContent = "Войти";
            document.getElementById("authorizationRef").style.display = "block";
            document.getElementById("profileRef").style.display = "none";
            document.getElementById("myBundlesRef").style.display = "none";
            document.getElementById("uploadFileRef").style.display = "none";
            document.getElementById("logoutRef").style.display = "none";
        }
    } catch (error) {
        alert("Ошибка при проверке авторизации " + error.message);
    }
}

window.onload = checkAuth;