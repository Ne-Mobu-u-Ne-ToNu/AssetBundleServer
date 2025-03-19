function loadFooter() {
    fetch('/partials/footer.html')
        .then(response => response.text())
        .then(data => {
            document.getElementById('footer-placeholder').innerHTML = data;
            updateYear(); // Вызываем функцию для обновления года
        })
        .catch(error => console.error('Ошибка при загрузке футера:', error));
}

function updateYear() {
    const currentYear = new Date().getFullYear();
    document.getElementById('current-year').textContent = currentYear;
}

loadFooter();