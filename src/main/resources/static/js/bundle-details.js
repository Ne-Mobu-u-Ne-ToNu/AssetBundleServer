const bundleId = window.location.pathname.split('/').pop();

fetch(`/api/public/bundle/${bundleId}`)
    .then(res => {
        if (!res.ok) {
            return res.json().then(data => {
                throw new Error(data.error);
            });
        }
        return res.json();
    })
    .then(data => {
        document.title = data.name;
        document.getElementById('bundle-name').textContent = data.name;
        document.getElementById('bundle-description').textContent = data.description;
        document.getElementById('uploaded-by').textContent = 'Автор: ' + data.uploadedBy.username;
        document.getElementById('bundle-uploaded-at').textContent =
        'Загружено: ' + new Date(data.uploadedAt).toLocaleDateString();
        document.getElementById('bundle-price').textContent = data.price
        ? new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB' }).format(data.price)
        : "Бесплатно";
        document.getElementById('btn-add-to-cart').dataset.bundleId = bundleId;
        createCartButtonsLogic();

        const mainImage = document.getElementById('main-image');
        const thumbnailList = document.getElementById('thumbnail-list');
        const modal = document.getElementById('image-modal');
        const modalImage = document.getElementById('modal-image');
        const closeModal = document.querySelector('.close-modal');
        const modalInner = document.querySelector('.modal-inner');

        if (data.imagePaths.length > 0) {
            mainImage.src = `/thumbnails/${data.imagePaths[0]}`;
            mainImage.dataset.current = data.imagePaths[0];
        }

        data.imagePaths.forEach((path, index) => {
            const thumb = document.createElement('img');
            thumb.src = `/thumbnails/${path}`;
            if (index === 0) thumb.classList.add('active');

        thumb.addEventListener('click', () => {
            if (mainImage.dataset.current === path) return;

            document.querySelectorAll('.thumbnail-list img').forEach(img => img.classList.remove('active'));
            thumb.classList.add('active');

            mainImage.classList.add('fade-out');


            mainImage.addEventListener('transitionend', function handleFadeOut() {
                mainImage.removeEventListener('transitionend', handleFadeOut);
                mainImage.src = `/thumbnails/${path}`;
                mainImage.dataset.current = path;

                mainImage.onload = () => {
                    mainImage.classList.remove('fade-out');
                };
            });
        });

            thumbnailList.appendChild(thumb);
        });

        let currentScale = 2.0;

        modalImage.addEventListener('click', () => {
            if (!modal.classList.contains('zoomed')) {
                modal.classList.toggle('zoomed');
                currentScale = 2.0;
                modalImage.style.transform = `scale(${currentScale})`;
            } else {
                modal.classList.remove('zoomed');
                modalImage.style.transform = '';
            }
        });

        modalInner.addEventListener('mousemove', (e) => {
            if (!modal.classList.contains('zoomed')) return;

            const rect = modalInner.getBoundingClientRect();
            const offsetX = ((e.clientX - rect.left) / rect.width) * 100;
            const offsetY = ((e.clientY - rect.top) / rect.height) * 100;

            modalImage.style.transformOrigin = `${offsetX}% ${offsetY}%`;
        });

        modalInner.addEventListener('wheel', (e) => {
            if (!modal.classList.contains('zoomed')) return;

            e.preventDefault();

            const scaleStep = 0.2;
            const minScale = 1;
            const maxScale = 4;

            if (e.deltaY < 0) {
                currentScale = Math.min(currentScale + scaleStep, maxScale);
            } else {
                currentScale = Math.max(currentScale - scaleStep, minScale);
            }

            modalImage.style.transform = `scale(${currentScale})`;
        });

        mainImage.addEventListener('click', () => {
            modal.style.display = 'block';
            modal.classList.remove('zoomed');
            modalImage.style.transform = '';
            modalImage.src = mainImage.src;
        });

        closeModal.addEventListener('click', () => {
            modal.classList.remove('zoomed');
            modalImage.style.transform = '';
            modal.style.display = 'none';
        });

        window.addEventListener('click', e => {
            if (e.target === modalInner) {
                modal.classList.remove('zoomed');
                modalImage.style.transform = '';
                modal.style.display = 'none';
            }
        });

        mainImage.addEventListener('load', () => {
            mainImage.classList.add('loaded');
        });

        window.addEventListener('keydown', e => {
            if (e.key === 'Escape' && modal.style.display === 'block') {
                modal.classList.remove('zoomed');
                modalImage.style.transform = '';
                modal.style.display = 'none';
            }
        });
    })
    .catch(error => {
        document.title = "Бандл не найден!";
});