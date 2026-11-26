# Frontend del e-commerce

Sitio estático basado en el proyecto de Front-End JS compartido. El catálogo consulta `http://localhost:8081/api/articulos`; el carrito se conserva en `localStorage`.

## Ejecución local

1. Iniciá primero el backend siguiendo [`../api/README.md`](../api/README.md).
2. Abrí `index.html` con la extensión **Live Server** de VS Code, o serví esta carpeta desde un servidor HTTP local.

La entidad actual de la API solo devuelve `id`, `nombre` y `precio`; por eso las imágenes y descripciones se muestran con valores de reemplazo. El carrito funciona en el navegador, pero el botón de compra todavía no crea pedidos en MySQL. Para completar el flujo indicado en la consigna hay que ampliar la API con atributos de producto y endpoints de pedidos.
