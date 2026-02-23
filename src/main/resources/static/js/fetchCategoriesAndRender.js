let categoriesCache = [];
const clearCategoriesBtn = document.getElementById('clear-categories');
let checkboxMap = new Map();

async function fetchCategoriesAndRender(categoriesTreeEl) {
    categoriesTreeEl.textContent = 'Загрузка...';
    try {
        const res = await fetch('/api/public/categories');
        if (!res.ok) throw new Error('Не удалось загрузить категории');
        const data = await res.json();
        categoriesCache = data;
        categoriesTreeEl.innerHTML = '';
        renderCategoryTree(categoriesCache, categoriesTreeEl);
    } catch (err) {
        categoriesTreeEl.textContent = 'Ошибка загрузки категорий';
    }
}

function renderCategoryTree(tree, container) {
    container.innerHTML = '';
    checkboxMap.clear();

    tree.forEach(node => {
        const nodeEl = createCategoryNode(node);
        container.appendChild(nodeEl);
    });
}

function createCategoryNode(node) {
    const wrapper = document.createElement('div');

    const row = document.createElement('div');
    row.className = 'category-node';

    const checkWrapper = document.createElement('label');
    checkWrapper.className = 'checkbox-wrapper';

    const cb = document.createElement('input');
    cb.type = 'checkbox';
    cb.className = 'cat-checkbox';
    cb.dataset.catId = String(node.id);

    const visual = document.createElement('span');
    visual.className = 'checkmark';

    const textSpan = document.createElement('span');
    textSpan.textContent = node.name;

    checkWrapper.appendChild(cb);
    checkWrapper.appendChild(visual);
    checkWrapper.appendChild(textSpan);

    checkboxMap.set(node.id, cb);

    cb.addEventListener('change', () => {
        onCheckboxChange(node, cb.checked);
    });

    row.appendChild(checkWrapper);
    wrapper.appendChild(row);

    if (node.subcategories && node.subcategories.length > 0) {
        const childrenWrap = document.createElement('div');
        childrenWrap.className = 'category-children';
        node.subcategories.forEach(child => {
            const childNode = createCategoryNode(child);
            childrenWrap.appendChild(childNode);
        });
        wrapper.appendChild(childrenWrap);
    }

    return wrapper;
}

function onCheckboxChange(node, checked) {
    setDescendantsChecked(node, checked);

    updateAncestorsState(node);
}

function setDescendantsChecked(node, checked) {
    const cb = checkboxMap.get(node.id);
    if (cb) {
        cb.checked = checked;
        cb.indeterminate = false;
        cb.closest('.checkbox-wrapper').classList.remove('indeterminate');
    }

    if (node.subcategories && node.subcategories.length > 0) {
        node.subcategories.forEach(child => setDescendantsChecked(child, checked));
    }
}

function updateAncestorsState(node) {
    function findPath(currentList, targetId, path = []) {
        for (const item of currentList) {
            const newPath = path.concat(item);
            if (item.id === targetId) return newPath;
            if (item.subcategories && item.subcategories.length > 0) {
                const res = findPath(item.subcategories, targetId, newPath);
                if (res) return res;
            }
        }
        return null;
    }

    const path = findPath(categoriesCache, node.id);
    if (!path) return;

    path.pop();

    for (let i = path.length - 1; i >= 0; i--) {
        const anc = path[i];
        updateNodeStateFromChildren(anc);
    }
}

function updateNodeStateFromChildren(node) {
    const cb = checkboxMap.get(node.id);
    if (!cb) return;

    const wrapper = cb.closest('.checkbox-wrapper');

    let total = 0, checkedCount = 0, indeterminateFound = false;
    function walkChildren(n) {
        if (!n.subcategories || n.subcategories.length === 0) {
            total++;
            const childCb = checkboxMap.get(n.id);
            if (childCb && childCb.checked) checkedCount++;
            if (childCb && childCb.indeterminate) indeterminateFound = true;
        } else {
            n.subcategories.forEach(s => walkChildren(s));
        }
    }

    if (node.subcategories && node.subcategories.length) {
        node.subcategories.forEach(s => walkChildren(s));
    }

    if (indeterminateFound) {
        cb.checked = false;
        cb.indeterminate = true;
        wrapper.classList.add('indeterminate');
    } else if (checkedCount === 0) {
        cb.checked = false;
        cb.indeterminate = false;
        wrapper.classList.remove('indeterminate');
    } else if (checkedCount === total) {
        cb.checked = true;
        cb.indeterminate = false;
        wrapper.classList.remove('indeterminate');
    } else {
        cb.checked = false;
        cb.indeterminate = true;
        wrapper.classList.add('indeterminate');
    }
}

function getSelectedCategoryIds() {
    const ids = [];
    for (const [id, cb] of checkboxMap.entries()) {
        if (cb.checked) ids.push(String(id));
    }
    return ids;
}

function applySelectedCategories(categories) {
    if (!categories) return;

    for (const cb of checkboxMap.values()) {
        cb.checked = false;
        cb.indeterminate = false;
        cb.closest('.checkbox-wrapper')?.classList.remove('indeterminate');
    }

    categories.forEach(cat => {
        const cb = checkboxMap.get(cat.id);
        if (cb) {
            cb.checked = true;
            cb.dispatchEvent(new Event('change', { bubbles: true }));
        }
    });
}

clearCategoriesBtn.addEventListener('click', () => {
    for (const cb of checkboxMap.values()) {
        cb.checked = false;
        cb.indeterminate = false;
        cb.closest('.checkbox-wrapper').classList.remove('indeterminate');

        cb.dispatchEvent(new Event('change', { bubbles: true }));
    }
});

