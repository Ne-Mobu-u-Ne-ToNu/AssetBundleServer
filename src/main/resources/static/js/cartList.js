async function removeFromCart(bundleId) {
    const response = await fetch(`/api/secured/cart/remove/${bundleId}`, {method: 'DELETE'});

    if (response.ok) {
        const data = await getCart();
        if (data && data.cartItems) {
            renderCardItems(data.cartItems, "cart", null);
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

window.addEventListener('load', async () => {
    const user = await checkAuth();
    const data = await getCart();
    if (data && data.cartItems) {
        renderCardItems(data.cartItems, "cart", user.role);
    }
});