document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById('uploadForm');
    const bundleInput = document.getElementById('bundleInput');
    const imageInput = document.getElementById('imageInput');
    const bundleFileName = document.getElementById('bundleFileName');
    const imageList = document.getElementById('imageList');

    const bundleDrop = document.getElementById('bundle-drop-area');
    const imageDrop = document.getElementById('image-drop-area');

    function preventDefaults(e) {
        e.preventDefault();
        e.stopPropagation();
    }

    function handleDragOver(dropArea) {
        dropArea.addEventListener("dragover", e => {
            preventDefaults(e);
            dropArea.classList.add("dragover");
        });

        dropArea.addEventListener("dragleave", () => {
            dropArea.classList.remove("dragover");
        });

        dropArea.addEventListener("drop", e => {
            preventDefaults(e);
            dropArea.classList.remove("dragover");

            const files = e.dataTransfer.files;
            if (dropArea === bundleDrop && files.length === 1) {
                bundleInput.files = files;
                bundleFileName.textContent = files[0].name;
            }

            if (dropArea === imageDrop) {
                imageInput.files = files;
                renderImageList(files);
            }
        });
    }

    function renderImageList(files) {
        imageList.innerHTML = '';
        for (const file of files) {
            const li = document.createElement('li');
            li.textContent = file.name;
            imageList.appendChild(li);
        }
    }

    bundleInput.addEventListener("change", () => {
        if (bundleInput.files.length) {
            bundleFileName.textContent = bundleInput.files[0].name;
        }
    });

    imageInput.addEventListener("change", () => {
        renderImageList(imageInput.files);
    });

    handleDragOver(bundleDrop);
    handleDragOver(imageDrop);

    form.addEventListener("submit", function (e) {
        e.preventDefault();

        const name = document.getElementById('name').value.trim();
        const description = document.getElementById('description').value.trim();
        const price = document.getElementById('price').value.trim();
        const bundleFile = bundleInput.files[0];
        const imageFiles = imageInput.files;

        if (!name || !description || !price || !bundleFile || imageFiles.length === 0) {
            alert("Пожалуйста, заполните все поля и выберите файлы.");
            return;
        }

        const formData = new FormData();
        formData.append("name", name);
        formData.append("description", description);
        formData.append("price", price);
        formData.append("bundleFile", bundleFile);
        for (let i = 0; i < imageFiles.length; i++) {
            formData.append("images", imageFiles[i]);
        }

            // Отправляем файл на сервер с помощью fetch
            fetch('/api/secured/upload', {
                method: 'POST',
                body: formData,
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
    });
});


