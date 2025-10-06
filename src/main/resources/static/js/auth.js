let userData = null;

async function checkAuth() {
    if (userData) return userData;

    try {
        const response = await fetch('/api/secured/user', {
            method: 'GET',
            credentials: 'include'
        });

        if (response.ok) {
            const data = await response.json();
            userData = data;

            document.getElementById("auth-link").textContent = data.username;
            document.getElementById("authorizationRef").style.display = "none";
            document.getElementById("profileRef").style.display = "block";
            document.getElementById("myBundlesRef").style.display = "block";
            if (data.role === "DEVELOPER") {
                document.getElementById("uploadFileRef").style.display = "block";
            } else {
                document.getElementById("uploadFileRef").style.display = "none";
            }

            if (data.role === "USER") {
                document.getElementById("cartRef").style.display = "block";
            } else {
                document.getElementById("cartRef").style.display = "none";
            }

            document.getElementById("logoutRef").style.display = "block";

            return data;
        } else {
            document.getElementById("auth-link").textContent = "Войти";
            document.getElementById("authorizationRef").style.display = "block";
            document.getElementById("profileRef").style.display = "none";
            document.getElementById("myBundlesRef").style.display = "none";
            document.getElementById("uploadFileRef").style.display = "none";
            document.getElementById("logoutRef").style.display = "none";
            document.getElementById("cartRef").style.display = "none";

            return null;
        }
    } catch (error) {
        alert("Ошибка при проверке авторизации " + error.message);
        return null;
    }
}

window.addEventListener('load', checkAuth);