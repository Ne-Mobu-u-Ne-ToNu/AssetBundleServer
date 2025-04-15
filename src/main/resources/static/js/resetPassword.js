document.getElementById('forgotPasswordForm').addEventListener('submit', function(event) {
  event.preventDefault();

  const formData = new URLSearchParams();
  formData.append('email', document.getElementById('email').value);

  fetch('/api/public/auth/resetPassword/request', {
        method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
        body: formData,
      })
      .then(response => {
            if (!response.ok) {
              return response.json().then(data => {
                throw new Error(data.error || 'Сообщение не отправилось');
              });
            }
            return response.json();
          })
          .then(data => {
              alert(data.message);
              window.location.href = "/authorization";
          })
          .catch(error => {
            alert(error.message);
          });
});