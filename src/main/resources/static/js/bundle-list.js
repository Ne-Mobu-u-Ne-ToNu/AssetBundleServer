let allBundles = [];
const bundle_search = document.getElementById("bundle-search");
const sortSelect = document.getElementById("sort-select");
const max_elem_page = document.getElementById("max-elem-page");

function showBundles(bundles) {
  const container = document.getElementById("bundle-list");
  container.innerHTML = "";

  bundles.forEach(bundle => {
    const card = document.createElement("div");
    card.className = "bundle-card";

    const cartButton = document.createElement("button");
    cartButton.className = "btn-add-to-cart";
    cartButton.dataset.bundleId = bundle.id;
    cartButton.textContent = "🛒 Добавить в корзину";

    const buyWrapper = document.createElement("div");
    buyWrapper.className = "buy-wrapper";
    buyWrapper.appendChild(cartButton);


    const title = document.createElement("h3");
    title.className = "bundle-title";
    title.innerHTML = `<a href="/bundle/${bundle.id}">${bundle.name}</a>`;

    const description = document.createElement("p");
    description.textContent = bundle.description;

    const wrapper = document.createElement("div");
    wrapper.className = "image-wrapper";

    const images = [];
    const dots = [];

    bundle.imagePaths.forEach((path, index) => {
      const img = document.createElement("img");
      img.src = `/thumbnails/${path}`;
      img.alt = `Image ${index}`;
      if (index === 0) img.classList.add("active");
      wrapper.appendChild(img);
      images.push(img);
    });

    const indicators = document.createElement("div");
    indicators.className = "dot-indicators";
    images.forEach((_, idx) => {
      const dot = document.createElement("div");
      dot.className = "dot" + (idx === 0 ? " active" : "");
      indicators.appendChild(dot);
      dots.push(dot);
    });

    let currentIndex = 0;

    wrapper.addEventListener("mousemove", (e) => {
      const rect = wrapper.getBoundingClientRect();
      const percent = (e.clientX - rect.left) / rect.width;
      const newIndex = Math.floor(percent * images.length);

      if (newIndex !== currentIndex && images[newIndex]) {
        images[currentIndex].classList.remove("active");
        dots[currentIndex].classList.remove("active");

        images[newIndex].classList.add("active");
        dots[newIndex].classList.add("active");

        currentIndex = newIndex;
      }
    });

    wrapper.addEventListener("mouseleave", () => {
      images[currentIndex].classList.remove("active");
      dots[currentIndex].classList.remove("active");
      currentIndex = 0;
      images[0].classList.add("active");
      dots[0].classList.add("active");
    });

    card.appendChild(title);
    card.appendChild(description);
    card.appendChild(wrapper);
    card.appendChild(indicators);
    card.appendChild(buyWrapper);

    container.appendChild(card);

    createCartButtonsLogic();
  });
}

let currentPage = 0;

function fetchBundles() {
  const query = bundle_search.value.toLowerCase();
  const params = new URLSearchParams({
    page: currentPage,
    size: parseInt(max_elem_page.value),
    sort: sortSelect.value,
    name: query
  });

  fetch(`/api/public/search?${params}`)
    .then(res => res.json())
    .then(data => {
    showBundles(data.bundles);
    renderPagination(data.totalPages, data.currentPage);
  });
}

fetchBundles();

let debounceTimer;
bundle_search.addEventListener("input", function () {
  clearTimeout(debounceTimer);

  debounceTimer = setTimeout(() => {
    fetchBundles();
  }, 500);
})

sortSelect.addEventListener("change", fetchBundles);

max_elem_page.addEventListener("change", fetchBundles)

function goToPage(page) {
  currentPage = page;
  fetchBundles();
}

function renderPagination(totalPages, currentPage) {
  const container = document.getElementById("pagination");
  container.innerHTML = "";

  const createBtn = (text, page, disabled = false, isActive = false) => {
    const btn = document.createElement("button");
    btn.textContent = text;
    if (disabled) btn.disabled = true;
    if (isActive) btn.classList.add("active");
    btn.addEventListener("click", () => goToPage(page));
    return btn;
  };

  // На первую и предыдущую
  container.appendChild(createBtn("<<", 0, currentPage === 0));
  container.appendChild(createBtn("<", currentPage - 1, currentPage === 0));

  const maxVisiblePages = 5;
  let start = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
  let end = Math.min(totalPages, start + maxVisiblePages);

  if (end - start < maxVisiblePages) {
    start = Math.max(0, end - maxVisiblePages);
  }

  if (start > 0) {
    container.appendChild(createBtn("1", 0));
    if (start > 1) {
      const dots = document.createElement("span");
      dots.textContent = "...";
      dots.classList.add("pagination-dots");
      container.appendChild(dots);
    }
  }

  for (let i = start; i < end; i++) {
    container.appendChild(createBtn(i + 1, i, false, i === currentPage));
  }

  if (end < totalPages) {
    if (end < totalPages - 1) {
      const dots = document.createElement("span");
      dots.textContent = "...";
      dots.classList.add("pagination-dots");
      container.appendChild(dots);
    }
    container.appendChild(createBtn(totalPages, totalPages - 1));
  }

  // На следующую и последнюю
  container.appendChild(createBtn(">", currentPage + 1, currentPage === totalPages - 1));
  container.appendChild(createBtn(">>", totalPages - 1, currentPage === totalPages - 1));
}