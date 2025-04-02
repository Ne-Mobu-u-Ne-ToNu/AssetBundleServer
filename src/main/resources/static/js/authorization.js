document.getElementById('signInForm').addEventListener('submit', function(event) {
  setMessageVisibility(false);
  event.preventDefault();

  const formData = {
    username: document.getElementById('userName').value,
    password: document.getElementById('passw').value
  };

  fetch('/api/public/auth/signin', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      })
      .then(response => {
            if (!response.ok) {
              return response.json().then(data => {
                throw new Error(data.error || 'Авторизация провалилась');
              });
            }
            return response.json();
          })
          .then(data => {
            if (data.message === "Неверные имя пользователя или пароль") {
              throw new Error(data.message);
            } else {
              window.location.href = "/";
            }
          })
          .catch(error => {
            setMessageText(error.message);
          });
});