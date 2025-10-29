async function getMyBundles() {
    const response = await fetch('/api/secured/myBundles', {method: 'GET'});

    if (response.ok) {
        const data = await response.json();
        return data;
    } else {
        return null;
    }
}

async function deleteBundle(bundleId) {
    const response = await fetch(`/api/secured/delete/${bundleId}`, { method: 'DELETE' });
    const data = await response.json();

    if (response.ok) {
        alert(data.message);
        window.location.reload();
    } else {
        alert(data.error);
    }
}