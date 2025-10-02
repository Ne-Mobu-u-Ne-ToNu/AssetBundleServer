const cartList = document.getElementById("cart-list");

function renderCart(bundlesInCart) {
    cartList.innerHTML = "";

    const checkoutBtn = document.getElementById("checkout-btn");
    const cartText = document.getElementById("cart-text");

    if (bundlesInCart.length > 0) {
        checkoutBtn.style.display = "block";
        cartText.textContent = "Ваша корзина"
    } else {
        checkoutBtn.style.display = "none";
        cartText.textContent = "Ваша корзина пуста"
    }

    bundlesInCart.forEach(bundle => {
        const card = document.createElement("div");
        card.className = "cart-item-card";

        const left = document.createElement("div");
        left.className = "cart-item-left";

        const img = document.createElement("img");
        img.src = `/thumbnails/${bundle.imagePaths[0]}`;
        img.alt = bundle.name;

        const link = document.createElement("a");
        link.href = `/bundle/${bundle.id}`;
        link.textContent = bundle.name;

        left.appendChild(img);
        left.appendChild(link);

        const right = document.createElement("div");
        right.className = "cart-item-right";

        const removeBtn = document.createElement("button");
        removeBtn.textContent = "Удалить";
        removeBtn.addEventListener("click", () => {
            removeFromCart(bundle.id);
        });

        right.appendChild(removeBtn);

        card.appendChild(left);
        card.appendChild(right);

        cartList.appendChild(card);
    });
}

async function removeFromCart(bundleId) {
    const response = await fetch(`/api/secured/cart/remove/${bundleId}`, {method: 'DELETE'});

    if (response.ok) {
        const data = await getCart();
        if (data && data.cartItems) {
            renderCart(data.cartItems);
        }
    }
}

window.addEventListener('load', async () => {
    const data = await getCart();
    if (data && data.cartItems) {
        renderCart(data.cartItems);
    }
});