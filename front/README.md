# Frontend de gestión de artículos

Interfaz responsive en HTML, CSS y JavaScript con tema oscuro e iconos. Se conecta a la API REST local para listar, crear, editar y eliminar artículos. La API actual admite los campos `nombre` y `precio`; el carrito se conserva en `localStorage`.

## Ejecución local

1. Iniciá MySQL y luego la API siguiendo [`../api/README.md`](../api/README.md).
2. Abrí esta carpeta en VS Code y ejecutá `index.html` con **Live Server**.
3. La interfaz consulta `http://localhost:8081/api/articulos`.

El flujo de pedidos aún no se registra en el backend: para eso es necesario implementar endpoints y persistencia de pedidos en la API.
