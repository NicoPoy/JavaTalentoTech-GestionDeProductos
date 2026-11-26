document.addEventListener("DOMContentLoaded", () => {
  const API = "http://localhost:8081/api";
  const currency = new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", maximumFractionDigits: 2 });
  const symbols = ["▧", "◈", "⌘", "◉", "◇", "▣", "✳", "◌"];
  const $ = (id) => document.getElementById(id);
  let products = [];
  let categories = [];
  let users = [];
  let orders = [];
  let productEditId = null;
  let identityEditId = null;
  let identityKind = "category";
  let toastTimer;
  let currentUser = null;
  let savedSession = readSession();
  let cart = { usuarioId: null, items: [], total: 0 };

  function readSession() {
    try { return JSON.parse(localStorage.getItem("mercado-user") || "null"); }
    catch { return null; }
  }

  function enterSession(user) {
    currentUser = user;
    savedSession = null;
    localStorage.setItem("mercado-user", JSON.stringify(user));
    $("login-screen").hidden = true;
    $("app-shell").hidden = false;
    $("current-avatar").textContent = (user.nombre || "U").trim().charAt(0).toLocaleUpperCase("es");
    $("current-user-name").textContent = user.nombre;
    $("current-user-email").textContent = user.email;
    $("checkout-current-user").textContent = `${user.nombre} · ${user.email}`;
    cart = { usuarioId: user.id, items: [], total: 0 };
    renderCart();
    updateCategorySelects();
    Promise.all([loadCart(), loadOrders()]);
  }

  function logout() {
    currentUser = null;
    localStorage.removeItem("mercado-user");
    $("app-shell").hidden = true;
    $("login-screen").hidden = false;
    $("login-email").value = "";
    $("login-error").textContent = "";
    cart = { usuarioId: null, items: [], total: 0 };
    renderCart();
  }

  function message(id, text, kind = "") {
    const element = $(id);
    element.textContent = text;
    element.className = `status-message${kind ? ` ${kind}` : ""}`;
  }

  function notify(text, error = false) {
    window.clearTimeout(toastTimer);
    $("toast-text").textContent = text;
    $("toast").classList.toggle("error", error);
    $("toast").classList.add("visible");
    toastTimer = window.setTimeout(() => $("toast").classList.remove("visible"), 3200);
  }

  async function api(path, options = {}) {
    const headers = { ...(options.headers || {}) };
    if (options.body) headers["Content-Type"] = "application/json";
    let response;
    try { response = await fetch(`${API}${path}`, { ...options, headers }); }
    catch { $("connection-label").textContent = "API desconectada"; throw new Error("No se pudo conectar con Spring Boot en el puerto 8081."); }
    if (!response.ok) {
      const detail = await response.text();
      let text = `La API respondió con HTTP ${response.status}.`;
      if (detail) {
        try { const data = JSON.parse(detail); text = data.detail || data.message || data.error || text; }
        catch { text = detail.slice(0, 220); }
      }
      throw new Error(text);
    }
    if (response.status === 204) return null;
    const body = await response.text();
    return body ? JSON.parse(body) : null;
  }

  async function loadCart() {
    if (!currentUser) return;
    const usuarioId = currentUser.id;
    try {
      const carrito = await api(`/carritos/${usuarioId}`);
      if (currentUser?.id !== usuarioId) return;
      cart = carrito; renderCart();
    }
    catch (error) { notify(`No se pudo cargar el carrito: ${error.message}`, true); }
  }

  $("login-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const email = $("login-email").value.trim();
    if (!$("login-email").checkValidity()) { $("login-error").textContent = "Ingresá un correo válido."; return; }
    $("login-error").textContent = "";
    try {
      const user = await api("/auth/login", { method: "POST", body: JSON.stringify({ email }) });
      enterSession(user);
    } catch (error) { $("login-error").textContent = `${error.message} Si todavía no existe, crealo abajo.`; }
  });

  $("register-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const payload = { nombre: $("register-name").value.trim(), email: $("register-email").value.trim() };
    if (!payload.nombre || !$("register-email").checkValidity()) { $("register-error").textContent = "Completá el nombre e ingresá un correo válido."; return; }
    $("register-error").textContent = "";
    try {
      const user = await api("/usuarios", { method: "POST", body: JSON.stringify(payload) });
      users = await api("/usuarios"); renderUsers(); enterSession(user);
    } catch (error) { $("register-error").textContent = error.message; }
  });

  $("logout-button").addEventListener("click", logout);

  function switchView(viewId) {
    document.querySelectorAll(".management-view").forEach((view) => { view.hidden = view.id !== viewId; });
    document.querySelectorAll(".side-nav [data-view]").forEach((button) => button.classList.toggle("active", button.dataset.view === viewId));
    const titles = { "products-view": "Productos", "categories-view": "Categorías", "users-view": "Usuarios", "orders-view": "Pedidos" };
    $("page-title").textContent = titles[viewId] || "Productos";
    if (viewId === "orders-view") loadOrders();
  }

  function updateStats() {
    const active = products.filter((product) => product.activo !== false);
    const low = active.filter((product) => Number(product.stock) <= 5);
    $("stat-total").textContent = String(active.length);
    $("stat-low-stock").textContent = String(low.length);
    const average = active.length ? active.reduce((sum, product) => sum + Number(product.precio), 0) / active.length : 0;
    $("stat-average").textContent = currency.format(average);
    $("orders-counter").textContent = String(orders.filter((order) => order.estado === "PENDIENTE").length);
  }

  function makeProductCard(product, index) {
    const card = document.createElement("article");
    card.className = "product-card";
    const top = document.createElement("div"); top.className = "product-card-top";
    const icon = document.createElement("span"); icon.className = "product-icon"; icon.setAttribute("aria-hidden", "true"); icon.textContent = symbols[index % symbols.length];
    const id = document.createElement("span"); id.className = "product-id"; id.textContent = `ID #${product.id}`;
    if (product.imagenUrl) {
      const image = document.createElement("img"); image.className = "product-image"; image.src = product.imagenUrl; image.alt = product.nombre; image.loading = "lazy"; image.referrerPolicy = "no-referrer";
      image.addEventListener("error", () => { image.remove(); top.prepend(icon); }, { once: true }); top.append(image, id);
    } else top.append(icon, id);
    const name = document.createElement("h3"); name.textContent = product.nombre; name.title = product.nombre;
    if (product.activo === false) { const archived = document.createElement("span"); archived.className = "archived-tag"; archived.textContent = "Archivado"; name.append(archived); }
    const price = document.createElement("p"); price.className = "product-price"; price.textContent = currency.format(Number(product.precio));
    const description = document.createElement("p"); description.className = "product-description"; description.textContent = product.descripcion || "Sin descripción";
    const meta = document.createElement("div"); meta.className = "stock-row";
    const category = document.createElement("span"); category.textContent = product.categoria || "Sin categoría";
    const stock = document.createElement("span"); stock.className = `stock-pill${product.stock === 0 ? " empty" : product.stock <= 5 ? " low" : ""}`; stock.textContent = `Stock: ${product.stock}`;
    meta.append(category, stock);
    const actions = document.createElement("div"); actions.className = "product-actions";
    if (product.activo !== false) {
      const add = document.createElement("button"); add.type = "button"; add.className = "small-button add-cart"; add.textContent = product.stock > 0 ? "+ Carrito" : "Sin stock"; add.disabled = product.stock <= 0; add.addEventListener("click", () => addToCart(product)); actions.append(add);
    } else {
      const reactivate = document.createElement("button"); reactivate.type = "button"; reactivate.className = "small-button add-cart"; reactivate.textContent = "Reactivar"; reactivate.addEventListener("click", () => reactivateProduct(product)); actions.append(reactivate);
    }
    const edit = document.createElement("button"); edit.type = "button"; edit.className = "small-button"; edit.textContent = "✎ Editar"; edit.addEventListener("click", () => openProductForm(product));
    const archive = document.createElement("button"); archive.type = "button"; archive.className = "small-button delete"; archive.textContent = product.activo === false ? "Archivado" : "⌫ Quitar"; archive.disabled = product.activo === false; archive.addEventListener("click", () => archiveProduct(product));
    actions.append(edit, archive); card.append(top, name, price, description, meta, actions); return card;
  }

  function renderProducts() {
    const includeArchived = $("show-archived").checked;
    const query = $("search-products").value.trim().toLocaleLowerCase("es");
    const visible = products.filter((product) => (includeArchived || product.activo !== false) &&
      (`${product.nombre} ${product.categoria || ""} ${product.id}`.toLocaleLowerCase("es").includes(query)));
    const grid = $("products-grid"); grid.replaceChildren(); updateStats();
    if (!visible.length) {
      const empty = document.createElement("div"); empty.className = "empty-state";
      const glyph = document.createElement("span"); glyph.className = "empty-icon"; glyph.textContent = query ? "⌕" : "▦";
      const title = document.createElement("strong"); title.textContent = query ? "No hay coincidencias" : "Todavía no hay productos";
      const text = document.createElement("p"); text.textContent = query ? "Probá con otro nombre, categoría o ID." : "Agregá un producto para comenzar el catálogo.";
      empty.append(glyph, title, text); grid.append(empty);
    } else visible.forEach((product, index) => grid.append(makeProductCard(product, index)));
    const low = products.filter((product) => product.activo !== false && product.stock <= 5);
    if (low.length) {
      const alert = document.createElement("div"); alert.className = "stock-alert";
      alert.textContent = `Aviso de stock bajo: ${low.map((product) => `${product.nombre} (${product.stock})`).join(" · ")}`;
      grid.prepend(alert);
    }
  }

  async function loadProducts() {
    message("products-status", "Cargando catálogo…");
    try {
      products = await api(`/productos?incluirInactivos=${$("show-archived").checked}`);
      $("connection-label").textContent = "API conectada · MySQL";
      renderProducts(); renderCategories(); message("products-status", "Catálogo actualizado.", "success");
      if (currentUser) await loadCart();
    } catch (error) { message("products-status", error.message, "error"); }
  }

  function openProductForm(product = null) {
    if (!categories.length) { notify("Creá una categoría antes de cargar productos.", true); switchView("categories-view"); return; }
    productEditId = product?.id ?? null;
    $("form-title").textContent = product ? "Editar producto" : "Nuevo producto";
    $("save-product").textContent = product ? "Guardar cambios" : "Guardar producto";
    $("product-name").value = product?.nombre || "";
    $("product-description").value = product?.descripcion || "";
    $("product-image-url").value = product?.imagenUrl || "";
    $("product-price").value = product?.precio ?? "";
    $("product-stock").value = product?.stock ?? 0;
    const select = $("product-category"); select.replaceChildren();
    categories.forEach((category) => { const option = new Option(category.nombre, category.id); select.add(option); });
    if (product) select.value = String(product.categoriaId);
    $("form-error").textContent = ""; $("product-modal").hidden = false; document.body.style.overflow = "hidden";
    window.setTimeout(() => $("product-name").focus(), 30);
  }

  function closeProductForm() { $("product-modal").hidden = true; document.body.style.overflow = ""; $("product-form").reset(); productEditId = null; }

  async function saveProduct(event) {
    event.preventDefault();
    const payload = { nombre: $("product-name").value.trim(), descripcion: $("product-description").value.trim(), imagenUrl: $("product-image-url").value.trim() || null, precio: Number($("product-price").value), stock: Number($("product-stock").value), categoriaId: Number($("product-category").value) };
    if (!payload.nombre || !Number.isFinite(payload.precio) || payload.precio <= 0 || !Number.isInteger(payload.stock) || payload.stock < 0 || !payload.categoriaId || !$("product-image-url").checkValidity()) { $("form-error").textContent = "Completá los campos y verificá precio, stock, categoría y URL de imagen."; return; }
    const editing = productEditId !== null; const id = productEditId; $("save-product").disabled = true; $("form-error").textContent = "";
    try {
      await api(editing ? `/productos/${id}` : "/productos", { method: editing ? "PUT" : "POST", body: JSON.stringify(payload) });
      closeProductForm(); await loadProducts(); notify(editing ? "Producto actualizado." : "Producto creado.");
    } catch (error) { $("form-error").textContent = error.message; }
    finally { $("save-product").disabled = false; $("save-product").textContent = editing ? "Guardar cambios" : "Guardar producto"; }
  }

  async function archiveProduct(product) {
    if (!window.confirm(`¿Quitar “${product.nombre}” del catálogo? Se conserva en el historial de pedidos.`)) return;
    try { await api(`/productos/${product.id}`, { method: "DELETE" }); await loadProducts(); notify("Producto archivado."); }
    catch (error) { notify(error.message, true); }
  }

  async function reactivateProduct(product) {
    try { await api(`/productos/${product.id}/reactivar`, { method: "PATCH" }); await loadProducts(); notify("Producto reactivado."); }
    catch (error) { notify(error.message, true); }
  }

  function renderCategories() {
    const list = $("categories-list"); list.replaceChildren();
    if (!categories.length) { list.append(emptyState("No hay categorías", "Creá una categoría para organizar los productos.")); return; }
    categories.forEach((category) => {
      const count = products.filter((product) => Number(product.categoriaId) === category.id).length;
      const row = simpleRow("◈", category.nombre, `${count} producto${count === 1 ? "" : "s"}`, [
        ["Editar", () => openIdentity("category", category), ""], ["Eliminar", () => deleteCategory(category), "delete"],
      ]);
      list.append(row);
    });
  }

  async function loadCategories() {
    try { categories = await api("/categorias"); renderCategories(); updateCategorySelects(); }
    catch (error) { message("categories-status", error.message, "error"); }
  }

  async function deleteCategory(category) {
    if (!window.confirm(`¿Eliminar la categoría “${category.nombre}”?`)) return;
    try { await api(`/categorias/${category.id}`, { method: "DELETE" }); await loadCategories(); notify("Categoría eliminada."); }
    catch (error) { notify(error.message, true); }
  }

  function renderUsers() {
    const list = $("users-list"); list.replaceChildren();
    if (!users.length) { list.append(emptyState("No hay usuarios", "Agregá un usuario antes de crear su primer pedido.")); return; }
    users.forEach((user) => {
      list.append(simpleRow("♙", user.nombre, user.email, [
        ["Editar", () => openIdentity("user", user), ""], ["Eliminar", () => deleteUser(user), "delete"],
      ]));
    });
  }

  async function loadUsers() {
    try {
      users = await api("/usuarios"); renderUsers(); updateCategorySelects();
      if (savedSession) {
        const user = users.find((entry) => entry.id === savedSession.id && entry.email.toLowerCase() === String(savedSession.email).toLowerCase());
        if (user) enterSession(user); else { savedSession = null; localStorage.removeItem("mercado-user"); }
      } else if (currentUser) {
        const user = users.find((entry) => entry.id === currentUser.id);
        if (user) enterSession(user); else logout();
      }
    }
    catch (error) { message("users-status", error.message, "error"); }
  }

  async function deleteUser(user) {
    if (!window.confirm(`¿Eliminar al usuario “${user.nombre}”?`)) return;
    try { await api(`/usuarios/${user.id}`, { method: "DELETE" }); await loadUsers(); notify("Usuario eliminado."); }
    catch (error) { notify(error.message, true); }
  }

  function updateCategorySelects() {
    const filterUser = $("filter-order-user");
    filterUser.replaceChildren(currentUser
      ? new Option(`Mi historial · ${currentUser.nombre}`, currentUser.id)
      : new Option("Todos los usuarios", ""));
    filterUser.value = currentUser ? String(currentUser.id) : "";
  }

  function openIdentity(kind, entity = null) {
    identityKind = kind; identityEditId = entity?.id ?? null;
    const isUser = kind === "user";
    $("identity-kicker").textContent = isUser ? "CLIENTES" : "ORGANIZACIÓN";
    $("identity-title").textContent = `${entity ? "Editar" : "Nueva"} ${isUser ? "usuario" : "categoría"}`;
    $("identity-description").textContent = isUser ? "El correo identifica al usuario para su historial de pedidos." : "Las categorías con productos asociados no se pueden eliminar.";
    $("identity-name").value = entity?.nombre || "";
    $("identity-email").value = entity?.email || "";
    $("identity-email-wrap").hidden = !isUser;
    $("identity-error").textContent = "";
    $("identity-modal").hidden = false; document.body.style.overflow = "hidden";
    window.setTimeout(() => $("identity-name").focus(), 30);
  }

  function closeIdentity() { $("identity-modal").hidden = true; document.body.style.overflow = ""; $("identity-form").reset(); identityEditId = null; }

  async function saveIdentity(event) {
    event.preventDefault();
    const isUser = identityKind === "user";
    const payload = isUser ? { nombre: $("identity-name").value.trim(), email: $("identity-email").value.trim() } : { nombre: $("identity-name").value.trim() };
    if (!payload.nombre || (isUser && !payload.email)) { $("identity-error").textContent = "Completá todos los campos requeridos."; return; }
    if (isUser && !$("identity-email").checkValidity()) { $("identity-error").textContent = "Ingresá un correo electrónico válido."; return; }
    const base = isUser ? "/usuarios" : "/categorias"; const editing = identityEditId !== null; const id = identityEditId;
    $("save-identity").disabled = true;
    try {
      await api(editing ? `${base}/${id}` : base, { method: editing ? "PUT" : "POST", body: JSON.stringify(payload) });
      closeIdentity();
      if (isUser) { await loadUsers(); await loadOrders(); } else { await loadCategories(); await loadProducts(); }
      notify(`${isUser ? "Usuario" : "Categoría"} ${editing ? "actualizado" : "creado"}.`);
    } catch (error) { $("identity-error").textContent = error.message; }
    finally { $("save-identity").disabled = false; }
  }

  function emptyState(titleText, descriptionText) {
    const box = document.createElement("div"); box.className = "empty-state";
    const icon = document.createElement("span"); icon.className = "empty-icon"; icon.textContent = "◌";
    const title = document.createElement("strong"); title.textContent = titleText;
    const description = document.createElement("p"); description.textContent = descriptionText;
    box.append(icon, title, description); return box;
  }

  function simpleRow(symbol, titleText, subtitle, actions) {
    const row = document.createElement("article"); row.className = "simple-row";
    const icon = document.createElement("span"); icon.className = "simple-row-icon"; icon.textContent = symbol;
    const copy = document.createElement("div"); copy.className = "simple-row-copy";
    const title = document.createElement("strong"); title.textContent = titleText;
    const detail = document.createElement("small"); detail.textContent = subtitle; copy.append(title, detail);
    const buttons = document.createElement("div"); buttons.className = "simple-row-actions";
    actions.forEach(([label, callback, type]) => { const button = document.createElement("button"); button.type = "button"; button.className = `small-button ${type || ""}`; button.textContent = label; button.addEventListener("click", callback); buttons.append(button); });
    row.append(icon, copy, buttons); return row;
  }

  async function loadOrders() {
    if (!currentUser) { orders = []; renderOrders(); updateStats(); return; }
    message("orders-status", "Cargando historial…");
    try {
      orders = await api(`/pedidos?usuarioId=${encodeURIComponent(currentUser.id)}`);
      renderOrders(); renderUsers(); updateStats(); message("orders-status", "Historial actualizado.", "success");
    } catch (error) { message("orders-status", error.message, "error"); }
  }

  const statusNames = { PENDIENTE: "Pendiente", CONFIRMADO: "Confirmado", ENVIADO: "Enviado", ENTREGADO: "Entregado", CANCELADO: "Cancelado" };
  function renderOrders() {
    const list = $("orders-list"); list.replaceChildren();
    const query = $("search-orders").value.trim().toLocaleLowerCase("es"); const state = $("filter-order-status").value;
    const visible = orders.filter((order) => (!state || order.estado === state) && (!query || String(order.id).includes(query)));
    if (!visible.length) { list.append(emptyState("No hay pedidos para mostrar", "Los pedidos creados desde el carrito aparecerán acá.")); return; }
    visible.forEach((order) => {
      const card = document.createElement("article"); card.className = "order-card";
      const top = document.createElement("div"); top.className = "order-top";
      const head = document.createElement("div"); const title = document.createElement("h3"); title.textContent = `Pedido #${order.id} · ${order.usuario}`;
      const time = document.createElement("p"); time.textContent = `${order.email} · ${new Date(order.fecha).toLocaleString("es-AR")}`; head.append(title, time);
      const total = document.createElement("strong"); total.className = "order-total"; total.textContent = currency.format(Number(order.total)); top.append(head, total);
      const lines = document.createElement("div"); lines.className = "order-lines";
      order.productos.forEach((line) => { const item = document.createElement("span"); item.textContent = `${line.cantidad} × ${line.producto} — ${currency.format(Number(line.subtotal))}`; lines.append(item); });
      const bottom = document.createElement("div"); bottom.className = "order-bottom";
      const badge = document.createElement("span"); badge.className = `status-badge status-${order.estado.toLowerCase()}`; badge.textContent = statusNames[order.estado] || order.estado;
      const actions = document.createElement("div"); actions.className = "order-actions";
      const next = { PENDIENTE: [["Confirmar", "CONFIRMADO"], ["Cancelar", "CANCELADO"]], CONFIRMADO: [["Marcar enviado", "ENVIADO"], ["Cancelar", "CANCELADO"]], ENVIADO: [["Marcar entregado", "ENTREGADO"]], ENTREGADO: [], CANCELADO: [] }[order.estado] || [];
      next.forEach(([label, newStatus]) => { const button = document.createElement("button"); button.type = "button"; button.className = `small-button${newStatus === "CANCELADO" ? " delete" : ""}`; button.textContent = label; button.addEventListener("click", () => changeOrderStatus(order, newStatus)); actions.append(button); });
      bottom.append(badge, actions); card.append(top, lines, bottom); list.append(card);
    });
  }

  async function changeOrderStatus(order, estado) {
    const prompts = { CONFIRMADO: "Confirmar pedido y descontar las unidades del stock?", CANCELADO: "Cancelar este pedido? Si ya estaba confirmado, se restituye el stock.", ENVIADO: "Marcar el pedido como enviado?", ENTREGADO: "Marcar el pedido como entregado?" };
    if (!window.confirm(prompts[estado])) return;
    try {
      await api(`/pedidos/${order.id}/estado`, { method: "PUT", body: JSON.stringify({ estado }) });
      await Promise.all([loadOrders(), loadProducts(), loadCart()]); notify(`Pedido #${order.id}: ${statusNames[estado].toLowerCase()}.`);
    } catch (error) { notify(error.message, true); }
  }

  async function addToCart(product) {
    if (!currentUser) { notify("Iniciá sesión para usar el carrito.", true); return; }
    try {
      cart = await api(`/carritos/${currentUser.id}/items`, {
        method: "POST", body: JSON.stringify({ productoId: product.id, cantidad: 1 }),
      });
      renderCart(); notify("Producto guardado en tu carrito.");
    } catch (error) { notify(error.message, true); }
  }

  async function changeCartQuantity(item, cantidad) {
    try {
      cart = await api(`/carritos/${currentUser.id}/items/${item.productoId}`, {
        method: "PUT", body: JSON.stringify({ cantidad }),
      });
      renderCart();
    } catch (error) { notify(error.message, true); }
  }

  async function removeCartItem(item) {
    try {
      cart = await api(`/carritos/${currentUser.id}/items/${item.productoId}`, { method: "DELETE" });
      renderCart(); notify("Producto quitado del carrito.");
    } catch (error) { notify(error.message, true); }
  }

  async function clearCart() {
    if (!currentUser) return;
    try {
      cart = await api(`/carritos/${currentUser.id}`, { method: "DELETE" });
      renderCart();
    } catch (error) { notify(error.message, true); }
  }

  function renderCart() {
    const items = cart?.items || [];
    const count = items.reduce((sum, item) => sum + item.cantidad, 0);
    $("cart-counter").textContent = String(count); $("cart-counter-nav").textContent = String(count); $("cart-counter-panel").textContent = String(count);
    $("cart-total").textContent = currency.format(Number(cart?.total || 0));
    const container = $("cart-items"); container.replaceChildren();
    if (!items.length) container.append(emptyState("Carrito vacío", "Agregá productos del catálogo para crear un pedido."));
    items.forEach((item) => {
      const row = document.createElement("article"); row.className = "cart-item";
      const icon = document.createElement("span"); icon.className = "cart-item-icon"; icon.textContent = "▧";
      const info = document.createElement("div"); info.className = "cart-item-info";
      const name = document.createElement("strong"); name.textContent = item.nombre;
      const detail = document.createElement("small"); detail.textContent = `${item.cantidad} × ${currency.format(Number(item.precioUnitario))} · Subtotal ${currency.format(Number(item.subtotal))}`;
      const quantity = document.createElement("div"); quantity.className = "cart-quantity";
      const decrease = document.createElement("button"); decrease.type = "button"; decrease.textContent = "−"; decrease.setAttribute("aria-label", `Restar una unidad de ${item.nombre}`); decrease.disabled = item.cantidad <= 1; decrease.addEventListener("click", () => changeCartQuantity(item, item.cantidad - 1));
      const countLabel = document.createElement("span"); countLabel.textContent = String(item.cantidad);
      const increase = document.createElement("button"); increase.type = "button"; increase.textContent = "+"; increase.setAttribute("aria-label", `Sumar una unidad de ${item.nombre}`); increase.addEventListener("click", () => changeCartQuantity(item, item.cantidad + 1));
      quantity.append(decrease, countLabel, increase); info.append(name, detail, quantity);
      const remove = document.createElement("button"); remove.type = "button"; remove.className = "cart-item-remove"; remove.textContent = "×"; remove.setAttribute("aria-label", `Quitar ${item.nombre}`); remove.addEventListener("click", () => removeCartItem(item));
      row.append(icon, info, remove);
      if (item.imagenUrl) {
        const image = document.createElement("img"); image.className = "cart-item-image"; image.src = item.imagenUrl; image.alt = item.nombre; image.loading = "lazy"; image.referrerPolicy = "no-referrer";
        image.addEventListener("error", () => { image.replaceWith(icon); }, { once: true }); row.replaceChild(image, icon);
      }
      container.append(row);
    });
    $("checkout-button").disabled = !items.length;
  }

  async function createOrder() {
    const usuarioId = currentUser?.id;
    if (!cart?.items?.length) return;
    if (!usuarioId) { notify("Iniciá sesión antes de crear un pedido.", true); return; }
    $("checkout-button").disabled = true;
    try {
      const order = await api("/pedidos/desde-carrito", { method: "POST", body: JSON.stringify({ usuarioId }) });
      cart = { usuarioId, items: [], total: 0 }; renderCart(); closeCart(); switchView("orders-view"); await loadOrders();
      notify(`Pedido #${order.id} creado como pendiente. Confirmalo para descontar el stock.`);
    } catch (error) { notify(error.message, true); }
    finally { $("checkout-button").disabled = !cart?.items?.length; }
  }

  function openCart() { $("cart-sidebar").classList.add("open"); $("cart-sidebar").setAttribute("aria-hidden", "false"); $("cart-overlay").classList.add("active"); }
  function closeCart() { $("cart-sidebar").classList.remove("open"); $("cart-sidebar").setAttribute("aria-hidden", "true"); $("cart-overlay").classList.remove("active"); }

  document.querySelectorAll(".side-nav [data-view]").forEach((button) => button.addEventListener("click", () => switchView(button.dataset.view)));
  $("new-product").addEventListener("click", () => openProductForm()); $("close-modal").addEventListener("click", closeProductForm); $("cancel-modal").addEventListener("click", closeProductForm); $("product-modal").addEventListener("click", (event) => { if (event.target === $("product-modal")) closeProductForm(); }); $("product-form").addEventListener("submit", saveProduct);
  $("search-products").addEventListener("input", renderProducts); $("show-archived").addEventListener("change", loadProducts); $("refresh-products").addEventListener("click", loadProducts);
  $("new-category").addEventListener("click", () => openIdentity("category")); $("new-user").addEventListener("click", () => openIdentity("user")); $("close-identity").addEventListener("click", closeIdentity); $("cancel-identity").addEventListener("click", closeIdentity); $("identity-modal").addEventListener("click", (event) => { if (event.target === $("identity-modal")) closeIdentity(); }); $("identity-form").addEventListener("submit", saveIdentity);
  $("refresh-orders").addEventListener("click", loadOrders); $("search-orders").addEventListener("input", renderOrders); $("filter-order-status").addEventListener("change", renderOrders); $("filter-order-user").addEventListener("change", loadOrders);
  $("open-cart").addEventListener("click", openCart); $("open-cart-top").addEventListener("click", openCart); $("close-cart").addEventListener("click", closeCart); $("cart-overlay").addEventListener("click", closeCart);
  $("clear-cart").addEventListener("click", clearCart); $("checkout-button").addEventListener("click", createOrder);
  document.addEventListener("keydown", (event) => { if (event.key === "Escape") { if (!$("product-modal").hidden) closeProductForm(); if (!$("identity-modal").hidden) closeIdentity(); closeCart(); } });

  renderCart(); updateCategorySelects();
  Promise.all([loadProducts(), loadCategories(), loadUsers(), loadOrders()]);
});
