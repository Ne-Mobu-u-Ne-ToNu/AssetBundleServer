document.getElementById('logoutRef').addEventListener('click', function(event) {
    event.preventDefault();

    fetch('/auth/logout', {
        method: 'POST',
        credentials: 'include'
    }).then(() => {
        window.location.href = '/'
    });
});