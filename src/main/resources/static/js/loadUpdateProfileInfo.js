const name = document.getElementById('name');
const email = document.getElementById('email');
const emailVerified = document.getElementById('emailVerified');
const verifyEmail = document.getElementById('verifyEmail');
const apiKey = document.getElementById('apiKey');
const role = document.getElementById('role');
const createdAt = document.getElementById('createdAt');
const saveBtn = document.getElementById('saveBtn');

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

document.getElementById('profileForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const formData = {
          newUsername: name.value,
          newEmail: email.value,
        };
        fetch('/api/secured/updateUser', {
          method: 'PUT',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(formData),
        })
        .then(response => {
              if (!response.ok) {
                return response.json().then(data => {
                  throw new Error(data.error || 'Не удалось обновить данные!');
                });
              }
              return response.json();
            })
            .then(data => {
                alert(data.message);
                window.location.reload();
            })
            .catch(error => {
              alert(error.message);
            });
});

document.getElementById('verifyEmail').addEventListener('click', function(event) {
    event.preventDefault();

        fetch('/api/secured/sendVerificationEmail', {
          method: 'POST',
          credentials: 'include',
        })
        .then(response => {
              if (!response.ok) {
                return response.json().then(data => {
                  throw new Error(data.error || 'Не удалось отправить сообщение!');
                });
              }
              return response.json();
            })
            .then(data => {
                alert(data.message);
            })
            .catch(error => {
              alert(error.message);
            });
});

document.getElementById('generateApiKey').addEventListener('click', function(event) {
    event.preventDefault();

        fetch('/api/secured/generateApiKey', {
          method: 'PUT',
          credentials: 'include',
        })
        .then(response => {
              if (!response.ok) {
                return response.json().then(data => {
                  throw new Error(data.error || 'Не удалось сгенерировать Api-ключ!');
                });
              }
              return response.json();
            })
            .then(data => {
                alert(data.message);
                window.location.reload();
            })
            .catch(error => {
              alert(error.message);
            });
});