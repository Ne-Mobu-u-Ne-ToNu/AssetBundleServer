document.getElementById('signInForm').addEventListener('submit', function(event) {
  event.preventDefault();

  const formData = {
    username: document.getElementById('userName').value,
    password: document.getElementById('passw').value
  };

  fetch('/auth/signin', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      })
      .then(response => {
            if (!response.ok) {
              return response.text().then(text => {
                throw new Error(text || 'Авторизация провалилась');
              });
            }
            return response.text();
          })
          .then(message => {
            if (message === "Не удалось авторизироваться. Неправильные имя пользователя или пароль") {
              throw new Error(message);
            } else {
              alert(message);
              //document.getElementById('message').textContent = message;
              //window.location.href = "/#";
            }
          })
          .catch(error => {
            alert(error.message);
            //document.getElementById('message').textContent = error.message;
          });
});