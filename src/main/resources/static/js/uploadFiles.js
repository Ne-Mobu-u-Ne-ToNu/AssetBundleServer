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

    async function getApiKey() {
        try {
            const response = await fetch('/api/secured/apiKey', {
                method: 'GET',
                credentials: 'include'
            });

            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.error || "Не удалось получить API-ключ!");
                })
            }

            const data = await response.json();
            return data.api_key;
        } catch (error) {
            alert(error.message);
            return null;
        }
    }

    async function uploadFile() {
        const files = fileInput.files;
        if (files.length === 0) {
            alert("Пожалуйста, выберите файл для загрузки.");
            return;
        }

        const apiKey = await getApiKey();
        if (!apiKey) {
            return;
        }

        const formData = new FormData();
        formData.append('file', files[0]);

        // Отправляем файл на сервер с помощью fetch
        fetch('/api/private/upload', {
            method: 'POST',
            body: formData,
            headers: {
                'X-API-KEY': apiKey
            }
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.error || 'Загрузка файла не удалась!');
                });
            }
            return response.json();
        })
        .then(data => {
            alert(data.message)
        })
        .catch(error => {
            alert(error.message);
        });
    }

    uploadButton.addEventListener('click', uploadFile);
});
