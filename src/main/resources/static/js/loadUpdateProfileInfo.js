const name = document.getElementById('name');
const email = document.getElementById('email');
const emailVerified = document.getElementById('emailVerified');
const verifyEmail = document.getElementById('verifyEmail');
const apiKey = document.getElementById('apiKey');
const role = document.getElementById('role');
const createdAt = document.getElementById('createdAt');
const saveBtn = document.getElementById('saveBtn');
const oldPassword = document.getElementById("olpPassw");
const password = document.getElementById("newPassw");
const confirmPassword = document.getElementById("passwConf");

async function loadInfo() {
    try {
        const response = await fetch('/api/secured/user', {
            method: 'GET',
            credentials: 'include'
        });

        if (response.ok) {
            const data = await response.json();

            name.value = data.username;
            email.value = data.email;

            if(data.email_verified) {
                emailVerified.textContent = "Да";
                verifyEmail.style.display = "none"
            } else {
                emailVerified.textContent = "Нет";
                verifyEmail.style.display = "block"
            }

            apiKey.textContent = data.api_key;

            if (data.role === "USER") {
                role.textContent = "Пользователь";
            } else if (data.role === "DEVELOPER") {
                role.textContent = "Разработчик";
            }

            const isoDate = data.created_at;
            const date = new Date(isoDate);

            const formatted = date.toLocaleString("ru-RU", {
                year: "numeric",
                month: "long",
                day: "numeric",
                hour: "2-digit",
                minute: "2-digit",
                second: "2-digit",
            });
            createdAt.textContent = formatted;
        }
    } catch (error) {
        alert("Ошибка при проверке авторизации " + error.message);
    }
}

window.addEventListener('load', loadInfo);

document.getElementById('editBtn').addEventListener('click', function() {
    this.style.display = 'none';
    name.removeAttribute('readonly');
    email.removeAttribute('readonly');
    saveBtn.style.display = "block";
});

function updateData(request, method, formData, errorMessage, onSuccessRedirect) {
            fetch(request, {
              method: method,
              credentials: 'include',
              headers: {
                'Content-Type': 'application/json',
              },
              body: JSON.stringify(formData),
            })
            .then(response => {
                  if (!response.ok) {
                    return response.json().then(data => {
                      throw new Error(data.error || errorMessage);
                    });
                  }
                  return response.json();
                })
                .then(data => {
                    alert(data.message);
                    if (typeof onSuccessRedirect === 'function') {
                        onSuccessRedirect();
                    }
                })
                .catch(error => {
                  alert(error.message);
                });
}

document.getElementById('profileForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const formData = {
          newUsername: name.value,
          newEmail: email.value,
        };

        updateData('/api/secured/updateUser', 'PUT', formData, 'Не удалось обновить данные!', () => {
            window.location.reload();
        });
});

document.getElementById('verifyEmail').addEventListener('click', function(event) {
    event.preventDefault();

    updateData('/api/secured/sendVerificationEmail', 'POST', null, 'Не удалось отправить сообщение!', () => {
        window.location.reload();
    });
});

document.getElementById('generateApiKey').addEventListener('click', function(event) {
    event.preventDefault();

        updateData('/api/secured/generateApiKey', 'PUT', null, 'Не удалось сгенерировать Api-ключ!', () => {
            window.location.reload();
        });
});

document.getElementById('deleteBtn').addEventListener('click', function(event) {
    event.preventDefault();

        updateData('/api/secured/deleteAccount', 'DELETE', null, 'Не удалось удалить аккаунт!', () => {
            window.location.href = "/";
        });
});

document.addEventListener("DOMContentLoaded", function() {
    password.addEventListener("input", validatePasswordMessage);
    confirmPassword.addEventListener("input", checkPasswordsMatch);
  });

document.getElementById('changePasswordForm').addEventListener('submit', function(event) {
    setMessageVisibility(false);
    event.preventDefault();

    if (checkPasswordsMatch() !== null) {
        return;
    }
    if (validatePasswordMessage().length !== 0) {
        return;
    }

    const formData = {
        oldPassword: oldPassword.value,
        newPassword: password.value,
        confPassword: confirmPassword.value,
    };

    updateData('/api/secured/updateUser', 'PUT', formData, 'Ну удалось сменить пароль!', () => {
        window.location.reload();
    });
});