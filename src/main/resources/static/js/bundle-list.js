let allBundles = [];

function showBundles(bundles) {
  const container = document.getElementById("bundle-list");
  container.innerHTML = "";

  bundles.forEach(bundle => {
    const card = document.createElement("div");
    card.className = "bundle-card";

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

    container.appendChild(card);
  });
}

fetch("/api/public/allBundles")
  .then(res => res.json())
  .then(data => {
    allBundles = data;
    showBundles(allBundles);
  });

let debounceTimer;
document.getElementById("bundle-search").addEventListener("input", function () {
  clearTimeout(debounceTimer);

  debounceTimer = setTimeout(() => {
    const query = this.value.toLowerCase();

    if (query.length == 0) {
      showBundles(allBundles);
      return;
    }

    fetch(`/api/public/search?name=${encodeURIComponent(query)}`)
      .then(res => res.json())
      .then(data => {
      showBundles(data);
    });
  }, 500);
})