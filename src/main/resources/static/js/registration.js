  const password = document.getElementById("passw");
  const confirmPassword = document.getElementById("passwConf");

  document.addEventListener("DOMContentLoaded", function() {
    password.addEventListener("input", validatePasswordMessage);
    confirmPassword.addEventListener("input", checkPasswordsMatch);
  });

  document.getElementById('signUpForm').addEventListener('submit', function(event) {
    setMessageVisibility(false);
    event.preventDefault();

    if (checkPasswordsMatch() !== null) {
        return;
    }
    if (validatePasswordMessage().length !== 0) {
        return;
    }

    const formData = {
      username: document.getElementById('userName').value,
      email: document.getElementById('email').value,
      password: password.value,
      confPassword: confirmPassword.value,
      role: document.querySelector('input[name="role"]:checked').value,
    };
    fetch('/api/public/auth/signup', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(formData),
    })
    .then(response => {
          if (!response.ok) {
            return response.json().then(data => {
              throw new Error(data.error || 'Регистрация провалилась');
            });
          }
          return response.json();
        })
        .then(data => {
            alert(data.message);
            window.location.href = "/authorization";
        })
        .catch(error => {
          setMessageVisibility(true);
          setMessageTextColor("red");
          setMessageText(error.message);
        });
  });