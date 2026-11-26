document.addEventListener("DOMContentLoaded", () => {
  const API_URL = "http://localhost:8081/api/articulos";
  const productsContainer = document.getElementById("productos-container");
  const productsStatus = document.getElementById("productos-status");
  const cartItems = document.getElementById("carrito-items");
  const cartSidebar = document.getElementById("cart-sidebar");
  const cartOverlay = document.getElementById("cart-overlay");
  const cartCount = document.getElementById("cart-counter");
  const navCartCount = document.getElementById("cart-counter-nav");
  const cartTotal = document.getElementById("carrito-total");
  const checkoutButton = document.getElementById("realizar-compra");
  const cartStorageKey = "cart";
  const fallbackImage =
    "https://dummyimage.com/600x400/e9ecef/495057.png&text=Articulo";

  function readCart() {
    try {
      const storedCart = JSON.parse(localStorage.getItem(cartStorageKey) || "[]");
      return Array.isArray(storedCart) ? storedCart : [];
    } catch {
      return [];
    }
  }

  let cart = readCart();

  function saveCart() {
    localStorage.setItem(cartStorageKey, JSON.stringify(cart));
    renderCart();
  }

  function createProductCard(articulo) {
    const name = articulo.nombre || "Artículo sin nombre";
    const price = Number(articulo.precio);
    const imageUrl = articulo.imagenUrl || articulo.imagen || fallbackImage;
    const description = articulo.descripcion || "Descripción no disponible.";

    const card = document.createElement("article");
    card.className = "card";

    const image = document.createElement("img");
    image.src = imageUrl;
    image.alt = name;
    image.className = "card-img-top";

    const body = document.createElement("div");
    body.className = "card-body";

    const heading = document.createElement("h3");
    heading.className = "card-title h5";
    heading.textContent = name;

    const shortDescription = document.createElement("p");
    shortDescription.className = "card-text";
    const words = description.split(/\s+/);
    shortDescription.textContent =
      words.slice(0, 8).join(" ") + (words.length > 8 ? "…" : "");

    const fullDescription = document.createElement("p");
    fullDescription.className = "card-text";
    fullDescription.textContent = description;
    fullDescription.hidden = true;

    const descriptionButton = document.createElement("button");
    descriptionButton.type = "button";
    descriptionButton.className = "btn btn-link";
    descriptionButton.textContent = "Ver descripción";
    descriptionButton.addEventListener("click", () => {
      fullDescription.hidden = !fullDescription.hidden;
      shortDescription.hidden = !fullDescription.hidden;
      descriptionButton.textContent = fullDescription.hidden
        ? "Ver descripción"
        : "Ocultar descripción";
    });

    const priceLabel = document.createElement("p");
    priceLabel.className = "card-text fw-bold";
    priceLabel.textContent = Number.isFinite(price)
      ? `$${price.toFixed(2)}`
      : "Precio no disponible";

    const addButton = document.createElement("button");
    addButton.type = "button";
    addButton.className = "btn btn-primary";
    addButton.textContent = "Agregar al carrito";
    addButton.disabled = !Number.isFinite(price) || price <= 0;
    addButton.addEventListener("click", () => {
      const existing = cart.find((item) => item.id === articulo.id);
      if (existing) {
        existing.quantity += 1;
      } else {
        cart.push({
          id: articulo.id,
          image: imageUrl,
          title: name,
          price,
          quantity: 1,
        });
      }
      saveCart();
      addButton.textContent = "Agregado";
      window.setTimeout(() => {
        addButton.textContent = "Agregar al carrito";
      }, 800);
    });

    body.append(
      heading,
      shortDescription,
      fullDescription,
      descriptionButton,
      priceLabel,
      addButton
    );
    card.append(image, body);
    return card;
  }

  async function loadProducts() {
    productsStatus.textContent = "Cargando artículos…";
    try {
      const response = await fetch(API_URL);
      if (!response.ok) {
        throw new Error(`La API respondió con HTTP ${response.status}`);
      }
      const articles = await response.json();
      if (!Array.isArray(articles)) {
        throw new Error("La respuesta no es una lista de artículos.");
      }

      productsContainer.replaceChildren(...articles.map(createProductCard));
      productsStatus.textContent = articles.length
        ? ""
        : "Todavía no hay artículos cargados.";
    } catch (error) {
      console.error("No se pudieron cargar los artículos:", error);
      productsStatus.textContent =
        "No se pudo conectar con la API. Verificá que Spring Boot esté activo en el puerto 8081.";
    }
  }

  function renderCart() {
    cartItems.replaceChildren();
    if (cart.length === 0) {
      const emptyMessage = document.createElement("p");
      emptyMessage.className = "text-center text-muted w-100";
      emptyMessage.textContent = "Tu carrito está vacío.";
      cartItems.append(emptyMessage);
      checkoutButton.disabled = true;
    } else {
      checkoutButton.disabled = false;
      cart.forEach((item) => {
        const row = document.createElement("article");
        row.className = "cart-item";

        const image = document.createElement("img");
        image.src = item.image || fallbackImage;
        image.alt = item.title;

        const details = document.createElement("div");
        details.className = "cart-item-details";

        const title = document.createElement("h3");
        title.className = "cart-item-title";
        title.textContent = item.title;

        const quantity = document.createElement("p");
        quantity.className = "cart-item-info";
        quantity.textContent = `Cantidad: ${item.quantity}`;

        const subtotal = document.createElement("p");
        subtotal.className = "cart-item-price";
        subtotal.textContent = `Subtotal: $${(item.price * item.quantity).toFixed(2)}`;

        const removeButton = document.createElement("button");
        removeButton.type = "button";
        removeButton.className = "btn btn-sm btn-danger cart-item-remove";
        removeButton.textContent = "Quitar";
        removeButton.addEventListener("click", () => {
          cart = cart.filter((cartItem) => cartItem.id !== item.id);
          saveCart();
        });

        details.append(title, quantity, subtotal, removeButton);
        row.append(image, details);
        cartItems.append(row);
      });
    }

    const count = cart.reduce((sum, item) => sum + item.quantity, 0);
    const total = cart.reduce(
      (sum, item) => sum + item.price * item.quantity,
      0
    );
    cartCount.textContent = String(count);
    navCartCount.textContent = String(count);
    cartTotal.textContent = total.toFixed(2);
  }

  document.getElementById("vaciar-carrito").addEventListener("click", () => {
    cart = [];
    saveCart();
  });

  document.getElementById("open-cart").addEventListener("click", (event) => {
    event.preventDefault();
    cartSidebar.classList.remove("hidden");
    cartOverlay.classList.add("active");
  });

  cartOverlay.addEventListener("click", () => {
    cartSidebar.classList.add("hidden");
    cartOverlay.classList.remove("active");
  });

  checkoutButton.addEventListener("click", () => {
    if (cart.length === 0) return;
    const total = cart.reduce(
      (sum, item) => sum + item.price * item.quantity,
      0
    );
    document.getElementById("modal-total").textContent = total.toFixed(2);
    new bootstrap.Modal(document.getElementById("compraExitosaModal")).show();
  });

  cartSidebar.classList.add("hidden");
  renderCart();
  loadProducts();
});
