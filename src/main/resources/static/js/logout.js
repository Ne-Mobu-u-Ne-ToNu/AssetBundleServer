document.getElementById('logoutRef').addEventListener('click', function(event) {
    event.preventDefault();

    fetch('/api/public/auth/logout', {
        method: 'POST',
        credentials: 'include'
    }).then(() => {
        window.location.href = '/'
    });
});