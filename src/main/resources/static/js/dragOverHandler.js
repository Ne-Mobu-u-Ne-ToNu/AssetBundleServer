function preventDefaults(e) {
    e.preventDefault();
    e.stopPropagation();
}

function handleDragOver(dropArea, bundleInput, imageInput, bundleDrop, imageDrop, imageList) {
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
            renderImageList(files, imageList);
        }
    });
}

function renderImageList(files, imageList) {
    imageList.innerHTML = '';
    for (const file of files) {
        const li = document.createElement('li');
        li.textContent = file.name;
        imageList.appendChild(li);
    }
}

function addDragOverEventListeners(bundleInput, bundleFileName, imageInput, imageList) {
    bundleInput.addEventListener("change", () => {
        if (bundleInput.files.length) {
            bundleFileName.textContent = bundleInput.files[0].name;
        }
    });

    imageInput.addEventListener("change", () => {
        renderImageList(imageInput.files, imageList);
    });
}