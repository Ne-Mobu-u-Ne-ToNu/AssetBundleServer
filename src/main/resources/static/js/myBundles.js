window.addEventListener('load', async () => {
    const user = await checkAuth();
    const data = await getMyBundles();
    if (data && data.myBundles) {
        renderCardItems(data.myBundles, "myBundles", user.role);
    }
});