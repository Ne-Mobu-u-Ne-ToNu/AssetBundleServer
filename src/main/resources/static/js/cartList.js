async function removeFromCart(bundleId) {
    const response = await fetch(`/api/secured/cart/remove/${bundleId}`, {method: 'DELETE'});

    if (response.ok) {
        const data = await getCart();
        if (data && data.cartItems) {
            renderCardItems(data.cartItems, "cart", null);
        }
    }
}

window.addEventListener('load', async () => {
    const user = await checkAuth();
    const data = await getCart();
    if (data && data.cartItems) {
        renderCardItems(data.cartItems, "cart", user.role);
    }
});