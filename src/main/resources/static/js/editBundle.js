const form = document.getElementById('uploadForm');
const bundleId = window.location.pathname.split('/').pop();
const categoryTreeEl = document.getElementById("categories-tree");
const bundleName = document.getElementById("name");
const bundleDesc = document.getElementById("description");
const bundlePrice = document.getElementById('price');
const bundleInput = document.getElementById('bundleInput');
const imageInput = document.getElementById('imageInput');
const imageList = document.getElementById('imageList');
const bundleDrop = document.getElementById('bundle-drop-area');
const imageDrop = document.getElementById('image-drop-area');


async function loadBundleInfo() {
    await fetchCategoriesAndRender(categoryTreeEl);

    handleDragOver(bundleDrop, bundleInput, null, bundleDrop, null, null);
    handleDragOver(imageDrop, null, imageInput, null, imageDrop, imageList);

    addDragOverEventListeners(bundleInput, bundleFileName, imageInput, imageList);

    try {
        const response = await fetch(`/api/public/bundle/${bundleId}`, {
            method: 'GET'
        });
        const data = await response.json();

        if (response.ok) {
            bundleName.value = data.name;
            bundleDesc.value = data.description;
            bundlePrice.value = data.price;
            applySelectedCategories(data.categories);

            form.addEventListener("submit", async function (e) {
                e.preventDefault();
                await sendUpdateRequest();
            });
        } else {
            throw new Error(data.error || "Ошибка запроса");
        }
    } catch (error) {
        alert("Ошибка при загрузке бандла " + error.message);
    }
}

async function sendUpdateRequest() {
    const name = document.getElementById('name').value.trim();
    const description = document.getElementById('description').value.trim();
    const price = document.getElementById('price').value.trim();
    const bundleFile = bundleInput.files[0];
    const imageFiles = imageInput.files;
    const categoryIds = getSelectedCategoryIds();

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

    try {
        const response = await fetch(`/api/secured/edit/${bundleId}`, {
            method: 'PUT',
            body: formData
        });
        const data = await response.json();

        if (response.ok) {
            alert(data.message);
            window.location.reload();
        } else {
            throw new Error(data.error || "Ошибка запроса");
        }
    } catch (error) {
        alert(error.message);
    }
}

window.addEventListener('load', loadBundleInfo);