function loadHeader() {
    fetch('/partials/header.html')
        .then(response => response.text())
        .then(data => {
            document.getElementById('header-placeholder').innerHTML = data;
        })
        .catch(error => console.error('Ошибка при загрузке хэдера:', error));
}

loadHeader();