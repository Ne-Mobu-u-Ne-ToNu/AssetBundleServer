document.getElementById('signUpForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const formData = {
      username: document.getElementById('userName').value,
      email: document.getElementById('email').value,
      password: document.getElementById('passw').value,
    };
    fetch('/auth/signup', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(formData),
    })
    .then(response => {
          if (!response.ok) {
            return response.text().then(text => {
              throw new Error(text || 'Регистрация провалилась');
            });
          }
          return response.text();
        })
        .then(message => {
          if (message === "Регистрация прошла успешно!") {
            alert(message);
            //document.getElementById('message').textContent = message;
            window.location.href = "/authorization";
          } else {
            throw new Error(message);
          }
        })
        .catch(error => {
          alert(error.message);
          //document.getElementById('message').textContent = error.message;
        });
  });