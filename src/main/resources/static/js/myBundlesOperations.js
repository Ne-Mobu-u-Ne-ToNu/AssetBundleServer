async function getMyBundles() {
    const response = await fetch('/api/secured/myBundles', {method: 'GET'});

    if (response.ok) {
        const data = await response.json();
        return data;
    } else {
        return null;
    }
}