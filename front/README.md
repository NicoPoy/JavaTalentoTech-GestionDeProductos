# Frontend de gestión de tienda

Interfaz responsive en HTML, CSS y JavaScript con tema oscuro e iconos, sin imágenes de producto. Permite administrar productos, categorías y usuarios; buscar artículos; controlar stock bajo; armar el carrito; crear pedidos; y consultar el historial por usuario y estado.

## Ejecución local

1. En MySQL Workbench, ejecutá `api/database/migrate_ecommerce.sql` sobre la conexión local. El script conserva `articulo`, crea las tablas de tienda y copia los artículos actuales a `producto` con stock inicial cero.
2. Iniciá la API siguiendo [`../api/README.md`](../api/README.md).
3. Abrí `front/index.html` con Live Server de VS Code. La API debe estar disponible en `http://localhost:8081`.

Los pedidos se crean como `PENDIENTE`. Al confirmarlos se vuelve a comprobar y descontar el stock; si se cancela un pedido confirmado, el stock se reintegra. El usuario es un registro de cliente para el historial; esta demo no implementa autenticación ni contraseñas.
