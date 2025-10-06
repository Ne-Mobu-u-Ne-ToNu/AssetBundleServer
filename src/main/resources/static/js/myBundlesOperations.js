async function getMyBundles() {
    const response = await fetch('/api/secured/myBundles', {method: 'GET'});

    if (response.ok) {
        const data = await response.json();
        return data;
    } else {
        return null;
    }
}

window.addEventListener('load', async () => {
    const user = await checkAuth();
    const data = await getMyBundles();
    if (data && data.myBundles) {
        renderCardItems(data.myBundles, "myBundles", user.role);
    }
});