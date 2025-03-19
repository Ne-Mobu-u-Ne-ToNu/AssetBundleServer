document.addEventListener("DOMContentLoaded", function() {
    const dropArea = document.getElementById('drop-area');
    const fileInput = document.getElementById('fileElem');
    const uploadButton = document.getElementById('uploadBtn');

    // Обработчики событий для drag-and-drop
    dropArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropArea.classList.add('hover');
    });

    dropArea.addEventListener('dragleave', () => {
        dropArea.classList.remove('hover');
    });

    dropArea.addEventListener('drop', (e) => {
        e.preventDefault();
        dropArea.classList.remove('hover');
        const files = e.dataTransfer.files;
        fileInput.files = files; // Присваиваем файлы в input
        displayFiles(files);
    });

    // Обработчик выбора файлов через input
    fileInput.addEventListener('change', () => {
        const files = fileInput.files;
        displayFiles(files); // Отображаем выбранные файлы
    });

    // Функция для отображения выбранных файлов в списке
    function displayFiles(files) {
        const fileList = document.getElementById('fileList');
        fileList.innerHTML = ''; // Очищаем список
        for (let i = 0; i < files.length; i++) {
            const li = document.createElement('li');
            li.textContent = files[i].name;
            fileList.appendChild(li);
        }
    }

    // Функция загрузки нескольких файлов на сервер
    function uploadFiles() {
        const files = fileInput.files; // Получаем выбранные файлы из input
        if (files.length === 0) {
            alert("Пожалуйста, выберите файлы для загрузки.");
            return;
        }

        const formData = new FormData();
        // Добавляем файлы в FormData
        for (let i = 0; i < files.length; i++) {
            formData.append('files', files[i]);
        }

        // Отправляем файлы на сервер с помощью fetch
        fetch('/api/upload/multiple', {
            method: 'POST',
            body: formData,
        })
        .then(response => response.text())
        .then(data => {
            alert('Файлы успешно загружены!');
        })
        .catch(error => {
            console.error('Ошибка:', error);
            alert('Произошла ошибка при загрузке файлов.');
        });
    }

    uploadButton.addEventListener('click', uploadFiles);
});
