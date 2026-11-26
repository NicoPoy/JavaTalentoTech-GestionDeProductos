document.addEventListener("DOMContentLoaded", () => {
  const API_URL = "http://localhost:8081/api/articulos";
  const grid = document.getElementById("products-grid");
  const status = document.getElementById("products-status");
  const searchInput = document.getElementById("search-products");
  const modal = document.getElementById("product-modal");
  const form = document.getElementById("product-form");
  const nameInput = document.getElementById("product-name");
  const priceInput = document.getElementById("product-price");
  const formError = document.getElementById("form-error");
  const saveButton = document.getElementById("save-product");
  const toast = document.getElementById("toast");
  const cartPanel = document.getElementById("cart-sidebar");
  const cartOverlay = document.getElementById("cart-overlay");
  const currency = new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", maximumFractionDigits: 2 });
  const iconChoices = ["▧", "◈", "⌘", "◉", "◇", "▣", "✳", "◌"];
  let articles = [];
  let editingId = null;
  let toastTimer;
  let cart = readCart();

  function readCart() {
    try {
      const value = JSON.parse(localStorage.getItem("mercado-cart") || "[]");
      return Array.isArray(value) ? value.filter((item) => item && Number.isFinite(Number(item.id)) && Number(item.price) > 0) : [];
    } catch {
      return [];
    }
  }

  function setStatus(message = "", type = "") {
    status.textContent = message;
    status.className = `status-message${type ? ` ${type}` : ""}`;
  }

  function notify(message, isError = false) {
    window.clearTimeout(toastTimer);
    document.getElementById("toast-text").textContent = message;
    toast.classList.toggle("error", isError);
    toast.classList.add("visible");
    toastTimer = window.setTimeout(() => toast.classList.remove("visible"), 3000);
  }

  async function request(url, options = {}) {
    const response = await fetch(url, {
      ...options,
      headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    });
    if (!response.ok) {
      const detail = await response.text();
      let message = `La API respondió con HTTP ${response.status}.`;
      if (detail) {
        try {
          const parsed = JSON.parse(detail);
          message = parsed.message || parsed.error || message;
        } catch {
          message = detail.slice(0, 180) || message;
        }
      }
      throw new Error(message);
    }
    if (response.status === 204) return null;
    const text = await response.text();
    return text ? JSON.parse(text) : null;
  }

  function openForm(article = null) {
    editingId = article ? article.id : null;
    document.getElementById("form-title").textContent = article ? "Editar artículo" : "Nuevo artículo";
    saveButton.textContent = article ? "Guardar cambios" : "Guardar artículo";
    nameInput.value = article?.nombre || "";
    priceInput.value = article?.precio ?? "";
    formError.textContent = "";
    modal.hidden = false;
    document.body.style.overflow = "hidden";
    window.setTimeout(() => nameInput.focus(), 30);
  }

  function closeForm() {
    modal.hidden = true;
    document.body.style.overflow = "";
    form.reset();
    editingId = null;
    formError.textContent = "";
  }

  function makeCard(article, index) {
    const card = document.createElement("article");
    card.className = "product-card";

    const top = document.createElement("div");
    top.className = "product-card-top";
    const icon = document.createElement("span");
    icon.className = "product-icon";
    icon.setAttribute("aria-hidden", "true");
    icon.textContent = iconChoices[index % iconChoices.length];
    const id = document.createElement("span");
    id.className = "product-id";
    id.textContent = `ID #${article.id}`;
    top.append(icon, id);

    const name = document.createElement("h3");
    name.title = article.nombre || "Artículo";
    name.textContent = article.nombre || "Artículo sin nombre";
    const price = document.createElement("p");
    price.className = "product-price";
    price.textContent = currency.format(Number(article.precio));
    const meta = document.createElement("p");
    meta.className = "product-meta";
    meta.textContent = "Artículo disponible";

    const actions = document.createElement("div");
    actions.className = "product-actions";
    const addButton = document.createElement("button");
    addButton.type = "button";
    addButton.className = "small-button add-cart";
    addButton.innerHTML = "<span aria-hidden=\"true\">＋</span> Carrito";
    addButton.addEventListener("click", () => addToCart(article));

    const editButton = document.createElement("button");
    editButton.type = "button";
    editButton.className = "small-button";
    editButton.innerHTML = "<span aria-hidden=\"true\">✎</span> Editar";
    editButton.addEventListener("click", () => openForm(article));

    const deleteButton = document.createElement("button");
    deleteButton.type = "button";
    deleteButton.className = "small-button delete";
    deleteButton.setAttribute("aria-label", `Eliminar ${article.nombre}`);
    deleteButton.innerHTML = "<span aria-hidden=\"true\">⌫</span>";
    deleteButton.addEventListener("click", () => deleteArticle(article));

    actions.append(addButton, editButton, deleteButton);
    card.append(top, name, price, meta, actions);
    return card;
  }

  function renderArticles() {
    const query = searchInput.value.trim().toLocaleLowerCase("es");
    const visible = articles.filter((article) =>
      String(article.nombre || "").toLocaleLowerCase("es").includes(query) || String(article.id).includes(query)
    );
    grid.replaceChildren();

    document.getElementById("stat-total").textContent = String(articles.length);
    document.getElementById("stat-visible").textContent = String(visible.length);
    const average = articles.length
      ? articles.reduce((sum, article) => sum + Number(article.precio || 0), 0) / articles.length
      : 0;
    document.getElementById("stat-average").textContent = articles.length ? currency.format(average) : currency.format(0);

    if (!visible.length) {
      const empty = document.createElement("div");
      empty.className = "empty-state";
      const glyph = document.createElement("span");
      glyph.className = "empty-icon";
      glyph.setAttribute("aria-hidden", "true");
      glyph.textContent = query ? "⌕" : "▦";
      const title = document.createElement("strong");
      title.textContent = query ? "No encontramos coincidencias" : "Todavía no hay artículos";
      const text = document.createElement("p");
      text.textContent = query ? "Probá con otro nombre o número de ID." : "Agregá tu primer artículo para empezar a armar el catálogo.";
      empty.append(glyph, title, text);
      grid.append(empty);
    } else {
      visible.forEach((article, index) => grid.append(makeCard(article, index)));
    }
  }

  async function loadArticles(showMessage = true) {
    if (showMessage) setStatus("Cargando catálogo…");
    document.getElementById("refresh-products").disabled = true;
    try {
      const result = await request(API_URL);
      if (!Array.isArray(result)) throw new Error("La API no devolvió una lista de artículos.");
      articles = result;
      renderArticles();
      setStatus(result.length ? "Catálogo actualizado." : "API conectada. Creá el primer artículo desde el botón de arriba.", result.length ? "success" : "");
    } catch (error) {
      console.error("No se pudo cargar el catálogo:", error);
      articles = [];
      renderArticles();
      setStatus("No se pudo conectar con la API. Confirmá que Spring Boot esté activo en http://localhost:8081.", "error");
    } finally {
      document.getElementById("refresh-products").disabled = false;
    }
  }

  async function saveArticle(event) {
    event.preventDefault();
    const nombre = nameInput.value.trim();
    const precio = Number(priceInput.value);
    if (!nombre) {
      formError.textContent = "Ingresá el nombre del artículo.";
      nameInput.focus();
      return;
    }
    if (!Number.isFinite(precio) || precio <= 0) {
      formError.textContent = "El precio debe ser mayor que cero.";
      priceInput.focus();
      return;
    }

    saveButton.disabled = true;
    saveButton.textContent = "Guardando…";
    formError.textContent = "";
    try {
      const isEditing = editingId !== null;
      const url = isEditing ? `${API_URL}/${encodeURIComponent(editingId)}` : API_URL;
      await request(url, { method: isEditing ? "PUT" : "POST", body: JSON.stringify({ nombre, precio }) });
      closeForm();
      await loadArticles(false);
      notify(isEditing ? "Artículo actualizado." : "Artículo creado.");
    } catch (error) {
      formError.textContent = error.message || "No se pudo guardar el artículo.";
    } finally {
      saveButton.disabled = false;
      saveButton.textContent = editingId !== null ? "Guardar cambios" : "Guardar artículo";
    }
  }

  async function deleteArticle(article) {
    if (!window.confirm(`¿Eliminar “${article.nombre}” del catálogo?`)) return;
    try {
      await request(`${API_URL}/${encodeURIComponent(article.id)}`, { method: "DELETE" });
      articles = articles.filter((item) => item.id !== article.id);
      renderArticles();
      setStatus("Catálogo actualizado.", "success");
      notify("Artículo eliminado.");
    } catch (error) {
      notify(error.message || "No se pudo eliminar el artículo.", true);
    }
  }

  function saveCart() {
    localStorage.setItem("mercado-cart", JSON.stringify(cart));
    renderCart();
  }

  function addToCart(article) {
    const existing = cart.find((item) => item.id === article.id);
    if (existing) existing.quantity += 1;
    else cart.push({ id: article.id, title: article.nombre, price: Number(article.precio), quantity: 1 });
    saveCart();
    notify("Agregado al carrito.");
  }

  function renderCart() {
    const count = cart.reduce((sum, item) => sum + item.quantity, 0);
    const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
    document.getElementById("cart-counter").textContent = String(count);
    document.getElementById("cart-counter-nav").textContent = String(count);
    document.getElementById("cart-counter-panel").textContent = String(count);
    document.getElementById("cart-total").textContent = currency.format(total);
    const container = document.getElementById("cart-items");
    container.replaceChildren();
    if (!cart.length) {
      const empty = document.createElement("div");
      empty.className = "cart-empty";
      empty.innerHTML = '<span aria-hidden="true">♧</span>Tu carrito está vacío.';
      container.append(empty);
    } else {
      cart.forEach((item) => {
        const row = document.createElement("article");
        row.className = "cart-item";
        const icon = document.createElement("span");
        icon.className = "cart-item-icon";
        icon.setAttribute("aria-hidden", "true");
        icon.textContent = "▧";
        const info = document.createElement("div");
        info.className = "cart-item-info";
        const title = document.createElement("strong");
        title.textContent = item.title;
        const detail = document.createElement("small");
        detail.textContent = `${item.quantity} × ${currency.format(item.price)}`;
        info.append(title, detail);
        const remove = document.createElement("button");
        remove.className = "cart-item-remove";
        remove.type = "button";
        remove.setAttribute("aria-label", `Quitar ${item.title}`);
        remove.textContent = "×";
        remove.addEventListener("click", () => {
          cart = cart.filter((cartItem) => cartItem.id !== item.id);
          saveCart();
        });
        row.append(icon, info, remove);
        container.append(row);
      });
    }
    document.getElementById("checkout-button").disabled = cart.length === 0;
  }

  function openCart() {
    cartPanel.classList.add("open");
    cartPanel.setAttribute("aria-hidden", "false");
    cartOverlay.classList.add("active");
  }

  function closeCart() {
    cartPanel.classList.remove("open");
    cartPanel.setAttribute("aria-hidden", "true");
    cartOverlay.classList.remove("active");
  }

  document.getElementById("new-product").addEventListener("click", () => openForm());
  document.getElementById("close-modal").addEventListener("click", closeForm);
  document.getElementById("cancel-modal").addEventListener("click", closeForm);
  modal.addEventListener("click", (event) => { if (event.target === modal) closeForm(); });
  form.addEventListener("submit", saveArticle);
  searchInput.addEventListener("input", renderArticles);
  document.getElementById("refresh-products").addEventListener("click", () => loadArticles());
  document.getElementById("open-cart").addEventListener("click", openCart);
  document.getElementById("open-cart-top").addEventListener("click", openCart);
  document.getElementById("close-cart").addEventListener("click", closeCart);
  cartOverlay.addEventListener("click", closeCart);
  document.getElementById("clear-cart").addEventListener("click", () => { cart = []; saveCart(); });
  document.getElementById("checkout-button").addEventListener("click", () => notify("La API todavía no tiene creación de pedidos.", true));
  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
      if (!modal.hidden) closeForm();
      closeCart();
    }
  });

  renderCart();
  loadArticles();
});
