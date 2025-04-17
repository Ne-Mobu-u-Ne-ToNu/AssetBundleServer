const verTokenHidden = document.getElementById("token");
const password = document.getElementById("passw");
const confirmPassword = document.getElementById("passwConf");

document.addEventListener("DOMContentLoaded", function() {
    password.addEventListener("input", validatePasswordMessage);
    confirmPassword.addEventListener("input", checkPasswordsMatch);
  });

document.getElementById('changeForgotPasswordForm').addEventListener('submit', function(event) {
  setMessageVisibility(false);
  event.preventDefault();

  if (checkPasswordsMatch() !== null) {
      return;
  }
  if (validatePasswordMessage().length !== 0) {
      return;
  }

  const formData = {
        verToken: verTokenHidden.value,
        newPassword: password.value,
        confPassword: confirmPassword.value,
      };

  fetch('/api/public/auth/resetPassword/confirm', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      })
      .then(response => {
            if (!response.ok) {
              return response.json().then(data => {
                throw new Error(data.error || 'Не удалось сменить пароль!');
              });
            }
            return response.json();
          })
          .then(data => {
              setMessageVisibility(true);
              setMessageTextColor("green");
              setMessageText(data.message);
          })
          .catch(error => {
            setMessageVisibility(true);
            setMessageTextColor("red");
            setMessageText(error.message);
          });
});