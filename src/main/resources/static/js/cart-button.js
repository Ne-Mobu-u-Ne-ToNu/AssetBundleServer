async function createCartButtonsLogic() {
   const user = await checkAuth();

   if (user === null || user.role !== "DEVELOPER") {
      const bought = await getMyBundles();
      document.querySelectorAll('.btn-add-to-cart').forEach(btn => {
         btn.style.display = "block";
         const bundleId = btn.dataset.bundleId;
         const myBundles = bought?.myBundles || [];

         if (!myBundles.some(b => b.id === parseInt(bundleId))) {
            fetch(`/api/secured/cart/check/${bundleId}`)
               .then(res => res.json().then(data => {
               if(res.ok) {
                  if (data.inCart) {
                     btn.textContent = "🛒 Удалить из корзины";
                     btn.dataset.inCart = "true";
                  } else {
                     btn.textContent = "🛒 Добавить в корзину";
                     btn.dataset.inCart = "false";
                  }
               }
            }))

            btn.onclick = () => {
               const inCart = btn.dataset.inCart === "true";
               const url = inCart ? `/api/secured/cart/remove/${bundleId}` : `/api/secured/cart/add/${bundleId}`;
               const method = inCart ? 'DELETE' : 'POST';
               const badge = document.getElementById("cart-badge");
               const current = inCart ? parseInt(badge.textContent) - 1 : parseInt(badge.textContent) + 1;

               fetch(url, { method: method})
                  .then(res => res.json().then(data => {
                  if (res.status === 401) {
                     window.location.href = "/authorization";
                     throw new Error(data.error);
                  }
                  if (!res.ok) {
                     throw new Error(data.error || 'Ошибка при редактировании корзины!');
                  }
                  return data;
               }))
                  .then(data => {
                  alert(data.message);
                  btn.dataset.inCart = (!inCart).toString();
                  btn.textContent = inCart ? "🛒 Добавить в корзину" : "🛒 Удалить из корзины";
                  updateCartBadge(current);
               })
                  .catch(err => alert(err.message));
            };
         } else {
            btn.textContent = "Куплен";
            btn.style.display = "block";
         }
      });
   }
}