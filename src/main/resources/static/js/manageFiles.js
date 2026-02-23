document.addEventListener("DOMContentLoaded", async () => {
    const form = document.getElementById('uploadForm');
    const bundleInput = document.getElementById('bundleInput');
    const imageInput = document.getElementById('imageInput');
    const bundleFileName = document.getElementById('bundleFileName');
    const imageList = document.getElementById('imageList');

    const bundleDrop = document.getElementById('bundle-drop-area');
    const imageDrop = document.getElementById('image-drop-area');

    const categoriesTreeEl = document.getElementById('categories-tree');

    await fetchCategoriesAndRender(categoriesTreeEl);

    handleDragOver(bundleDrop, bundleInput, null, bundleDrop, null, null);
    handleDragOver(imageDrop, null, imageInput, null, imageDrop, imageList);

    addDragOverEventListeners(bundleInput, bundleFileName, imageInput, imageList);

    form.addEventListener("submit", function (e) {
        e.preventDefault();

        const name = document.getElementById('name').value.trim();
        const description = document.getElementById('description').value.trim();
        const price = document.getElementById('price').value.trim();
        const bundleFile = bundleInput.files[0];
        const imageFiles = imageInput.files;
        const categoryIds = getSelectedCategoryIds();

        if (!name || !description || !price || !bundleFile || imageFiles.length === 0) {
            alert("Пожалуйста, заполните все поля и выберите файлы.");
            return;
        }

        const formData = new FormData();
        formData.append("name", name);
        formData.append("description", description);
        formData.append("price", price);
        if (bundleFile) {
            formData.append("bundleFile", bundleFile);
        }
        formData.append("categoryIds", categoryIds);

        if (imageFiles) {
            for (let i = 0; i < imageFiles.length; i++) {
                formData.append("images", imageFiles[i]);
            }
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
                alert(data.message);
                window.location.reload();
            })
            .catch(error => {
                alert(error.message);
            });
    });
});


