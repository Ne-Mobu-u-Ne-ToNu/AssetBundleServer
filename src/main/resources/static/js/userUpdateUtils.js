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