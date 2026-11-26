# Frontend de gestión de tienda

Interfaz responsive en HTML, CSS y JavaScript con tema oscuro e iconos. Permite ingresar por correo o crear un usuario de demostración, administrar productos (incluida una URL de imagen opcional), categorías y usuarios; buscar artículos; controlar stock bajo; administrar el carrito guardado en MySQL; crear pedidos; y consultar el historial del usuario activo.

## Ejecución local

1. En MySQL Workbench, ejecutá `api/database/migrate_ecommerce.sql` sobre la conexión local. El script prepara la base completa, conserva `articulo`, crea las tablas de tienda, copia artículos actuales a `producto` con stock inicial cero y agrega `demo@mercado.local` para el ingreso de demostración.
2. Iniciá la API siguiendo [`../api/README.md`](../api/README.md).
3. Abrí `front/index.html` con Live Server de VS Code. La API debe estar disponible en `http://localhost:8081`.

El carrito y sus cantidades se leen y guardan mediante la API en MySQL, asociados al usuario. Al crear un pedido, la API vuelve a validar el stock y solo vacía el carrito cuando se guarda el pedido correctamente. El pedido se crea como `PENDIENTE`; al confirmarlo se vuelve a comprobar y descontar el stock; si se cancela un pedido confirmado, el stock se reintegra. El acceso se identifica por correo y se recuerda en el navegador, sin contraseña ni protección de endpoints; sirve para la demostración, no como autenticación segura. `root/admin` corresponde solo a MySQL.
