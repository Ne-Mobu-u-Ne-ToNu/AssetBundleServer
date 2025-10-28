async function removeFromCart(bundleId) {
    const response = await fetch(`/api/secured/cart/remove/${bundleId}`, {method: 'DELETE'});

    if (response.ok) {
        const data = await getCart();
        if (data && data.cartItems) {
            renderCardItems(data.cartItems, "cart", null);
            updateTotalPrice(data.cartItems);
        }
    }
}

async function purchaseBundles(bundlesInCart) {
    if (bundlesInCart.length === 0) return;

    const bundleIds = bundlesInCart.map(b => b.id);

    try {
        const response = await fetch("/api/secured/purchase/all", {
            method: 'POST',
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(bundleIds)
        });

        if (response.ok) {
            alert("Бандлы успешно куплены!");
            window.location.reload();
        } else {
            const data = await response.json();
            alert(data.error);
        }
    } catch (e) {
        alert("Ошибка покупки " + e);
    }
}

function updateTotalPrice(bundlesInCard) {
    const totalPriceContainer = document.getElementById("total-price-container");

    if (bundlesInCard.length === 0) {
        totalPriceContainer.style.display = "none"
        return;
    }

    const totalPrice = document.getElementById("total-price");
    let total = 0;

    bundlesInCard.forEach(bundle => {
        total += bundle.price;
    });

    totalPrice.textContent = new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB' }).format(total);
    totalPriceContainer.style.display = "block";
}

window.addEventListener('load', async () => {
    const user = await checkAuth();
    const data = await getCart();
    if (data && data.cartItems) {
        renderCardItems(data.cartItems, "cart", user.role);
        updateTotalPrice(data.cartItems);
    }
});