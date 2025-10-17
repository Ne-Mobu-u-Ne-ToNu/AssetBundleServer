async function loadComments(bundleId) {
    const res = await fetch(`/api/public/bundles/${bundleId}/comments`);
    if (res.ok) {
        const comments = await res.json();
        const list = document.getElementById("comments-list");

        await renderComments(comments, list, bundleId);

        document.querySelectorAll(".delete-comment").forEach(btn => {
            btn.onclick = async () => {
                if (!confirm("Удалить комментарий?")) return;

                const commentId = btn.dataset.id;
                const res = await fetch(`/api/secured/comments/delete/${commentId}`, { method: "DELETE" });

                if (res.ok) {
                    loadComments(bundleId);
                } else {
                    if (res.status === 401) {
                        window.location.href = "/authorization";
                    }
                    alert((await res.json()).error);
                }
            };
        });
    }
}

async function renderComments(comments, container, bundleId) {
    container.innerHTML = "";
    comments.forEach(c => {
        const div = document.createElement("div");
        div.className = "comment";
        const edited = c.edited ? "ред." : "";

        let nameHtml = `<b>${c.authorName}</b>`
        if (c.bundleAuthor) {
            nameHtml += `<span class="badge-author">Автор</span>`;
            div.classList.add("author-comment");
        }

        let html = `${nameHtml} <span>(${new Date(c.createdAt).toLocaleString()}) ${edited}</span>
        <p>${c.text}</p>
        <button class="reply-comment" data-author="${c.authorName}" data-id="${c.id}">Ответить</button>`;

        if (c.author) {
            html += `<button class="edit-comment" data-id="${c.id}" data-text="${encodeURIComponent(c.text)}">Редактировать</button>
            <button class ="delete-comment" data-id="${c.id}">Удалить</button>`;
        }

        div.innerHTML = html;
        setupCommentActions(div, bundleId);

        container.appendChild(div);

        if (c.replies && c.replies.length > 0) {
            const repliesDiv = document.createElement("div");
            repliesDiv.className = "replies";
            renderComments(c.replies, repliesDiv, bundleId);
            div.appendChild(repliesDiv);
        }
    });
}

function setupCommentActions(commentDiv, bundleId) {
    commentDiv.querySelectorAll(".reply-comment").forEach(btn => {
        btn.onclick = () => openInlineForm(commentDiv, bundleId, {
            mode: "reply",
            parentId: btn.dataset.id,
            placeholder: `Ответить на комментарий @${btn.dataset.author}, `,
            text: `@${btn.dataset.author}, `,
            submitText: "Ответить"
        });
    });

    commentDiv.querySelectorAll(".edit-comment").forEach(btn => {
        btn.onclick = () => openInlineForm(commentDiv, bundleId, {
            mode: "edit",
            commentId: btn.dataset.id,
            placeholder: "Редактировать комментарий",
            text: decodeURIComponent(btn.dataset.text),
            submitText: "Сохранить"
        });
    });
}

function openInlineForm(commentDiv, bundleId, { mode, parentId, commentId, placeholder = "", text = "", submitText }) {
    commentDiv.querySelectorAll(".inline-form").forEach(f => f.remove());

    const formDiv = document.createElement("div");
    formDiv.className = "inline-form";
    formDiv.innerHTML = `
        <textarea class="inline-textarea" placeholder="${placeholder}">${text}</textarea>
        <div class="inline-buttons">
            <button class="submit-inline">${submitText}</button>
            <button class="cancel-inline">Отмена</button>
        </div>
    `;

    const textarea = formDiv.querySelector(".inline-textarea");
    const submitBtn = formDiv.querySelector(".submit-inline");
    const cancelBtn = formDiv.querySelector(".cancel-inline");

    cancelBtn.onclick = () => formDiv.remove();

    submitBtn.onclick = async () => {
        const content = textarea.value.trim();
        if (!content) return alert("Комментарий не может быть пустым!");

        let url, method, body;

        if (mode === "reply") {
            url = `/api/secured/bundles/${bundleId}/addComment`;
            method = "POST";
            body = JSON.stringify({ text: content, parentId })
        } else if (mode === "edit") {
            url = `/api/secured/comments/editComment/${commentId}`;
            method = "PUT";
            body = JSON.stringify({ text: content });
        }

        const res = await fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body
        });

        if (res.ok) {
            loadComments(bundleId);
        } else {
            if (res.status === 401) {
                window.location.href = "/authorization";
            }
            alert((await res.json()).error);
        }
    };

    commentDiv.appendChild(formDiv);
}

document.getElementById("comment-submit").addEventListener("click", async () => {
    const text = document.getElementById("comment-input").value.trim();
    if (!text) return alert("Комментарий не может быть пустым!");

    const bundleId = window.location.pathname.split("/").pop();

    const res = await fetch(`/api/secured/bundles/${bundleId}/addComment`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ text })
    });

    const data = await res.json();

    if (res.ok) {
        document.getElementById("comment-input").value = "";
        loadComments(bundleId);
    } else {
        if (res.status === 401) {
            window.location.href = "/authorization";
        }
        alert(data.error || "Ошибка при добавлении комментария");
    }
})

loadComments(window.location.pathname.split("/").pop());