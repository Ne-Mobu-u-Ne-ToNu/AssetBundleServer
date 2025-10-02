function updateCartBadge(count) {
    const badge = document.getElementById("cart-badge");
    if (count > 0) {
        badge.textContent = count;
        badge.style.display = "block";
    } else {
        badge.style.display = "none";
    }
}

async function getCart() {
    const response = await fetch('/api/secured/cart', {method: 'GET'});

    if (response.ok) {
        const data = await response.json();
        updateCartBadge(data.cartItems.length);
        return data;
    } else {
        updateCartBadge(0);
        return null;
    }
}

window.addEventListener('load', getCart);