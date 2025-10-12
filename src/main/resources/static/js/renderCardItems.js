const cardList = document.getElementById("card-list");

function renderCardItems(bundlesInCard, mode, role) {
    cardList.innerHTML = "";

    const checkoutBtn = document.getElementById("checkout-btn");
    const cardText = document.getElementById("card-text");

    if (mode === "cart") {
        if (bundlesInCard.length > 0) {
            checkoutBtn.style.display = "block";
            checkoutBtn.addEventListener("click", () => {
                purchaseBundles(bundlesInCard);
            });

            cardText.textContent = "Ваша корзина"
        } else {
            checkoutBtn.style.display = "none";
            cardText.textContent = "Ваша корзина пуста"
        }
    } else if (mode === "myBundles") {
        cardText.textContent = bundlesInCard.length > 0 ? "Ваши бандлы" : "У вас нет бандлов";
    }

    bundlesInCard.forEach(bundle => {
        const card = document.createElement("div");
        card.className = "card-item-card";

        const left = document.createElement("div");
        left.className = "card-item-left";

        const img = document.createElement("img");
        img.src = `/thumbnails/${bundle.imagePaths[0]}`;
        img.alt = bundle.name;

        const link = document.createElement("a");
        link.href = `/bundle/${bundle.id}`;
        link.textContent = bundle.name;

        left.appendChild(img);
        left.appendChild(link);

        const right = document.createElement("div");
        right.className = "card-item-right";

        if ((role === "USER" || role === "DEVELOPER") && mode != "cart") {
            const downloadBtn = document.createElement("button");
            downloadBtn.id = "download-btn";
            downloadBtn.textContent = "Скачать";
            downloadBtn.addEventListener("click", () => {
                // Скачивание бандла
            });
            right.appendChild(downloadBtn);
        }

        if (role === "DEVELOPER" && mode != "cart") {
            const editBtn = document.createElement("button");
            editBtn.id = "edit-btn";
            editBtn.textContent = "Редактировать";
            editBtn.addEventListener("click", () => {
                // Редактирование бандла
            });
            right.appendChild(editBtn);
        }

        if (role === "DEVELOPER" || mode === "cart") {
            const removeBtn = document.createElement("button");
            removeBtn.id = "delete-btn";
            removeBtn.textContent = "Удалить";
            removeBtn.addEventListener("click", () => {
                if (mode === "cart") {
                    removeFromCart(bundle.id);
                } else if (role === "DEVELOPER") {
                    // Удаление с сервера
                }
            });
            right.appendChild(removeBtn);
        }

        card.appendChild(left);
        card.appendChild(right);

        cardList.appendChild(card);
    });
}