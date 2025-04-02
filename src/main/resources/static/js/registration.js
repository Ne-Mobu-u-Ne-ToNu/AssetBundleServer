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
          setMessageTextColor("red");
          setMessageText(error.message);
        });
  });

  function validatePassword(password) {
    const errors = []

    if (password.length < 8) {
        errors.push("Длина пароля минимум 8 символов");
    }
    if (!/[A-Z]/.test(password)) {
        errors.push("Минимум одна прописная латинская буква");
    }
    if (!/[a-z]/.test(password)) {
        errors.push("Минимум одна строчная латинская буква");
    }
    if (!/\d/.test(password)) {
        errors.push("Минимум одна цифра");
    }
    if (!/[!\"$%&'()+,\-./:;<=>?@[\]^_{|}~`]/.test(password)) {
            errors.push("Пароль должен содержать хотя бы один специальный символ (!\"$%&'()+,-./:;<=>?@[]^_{|}~`).");
    }

    return errors;
  }

  function validatePasswordMessage() {
    errors = validatePassword(password.value);
    if (password.value.length > 0 && errors.length > 0) {
        setMessageVisibility(true);
        setMessageTextColor("red");
        setMessageText(errors[0]);
    } else {
        setMessageVisibility(false);
    }
    return errors;
  }

  function checkPasswordsMatch() {
      if (confirmPassword.value.length > 0) {
          setMessageVisibility(true);
          if (password.value !== confirmPassword.value) {
              error = "Пароли не совпадают!";
              errors.push(error);
              setMessageText(error);
              setMessageTextColor("red");
              return error;
          } else {
              setMessageText("Пароли совпадают");
              setMessageTextColor("green");
          }
      } else {
        setMessageVisibility(false);
      }
      return null;
  }