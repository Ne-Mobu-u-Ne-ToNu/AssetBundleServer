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

async function downloadBundle(bundleId) {
    const response = await fetch(`/api/secured/download/${bundleId}`);

    if (!response.ok) {
        const data = await response.json();
        alert(data.error || "Ошибка при скачивании файла");
        return;
    }

    const blob = await response.blob();

    const contentDisposition = response.headers.get("Content-Disposition");
    let filename = "bundle";

    if (contentDisposition) {
        const match = contentDisposition.match(/filename="(.+)"/);
        if (match && match[1]) {
            filename = match[1];
        }
    }

    const url = window.URL.createObjectURL(blob);

    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();

    a.remove();
    window.URL.revokeObjectURL(url);
}