# Frontend de gestión de tienda

Panel responsive en HTML, CSS y JavaScript, con tema oscuro e iconos. Permite gestionar productos, categorías, usuarios y stock; buscar en el catálogo; guardar productos en el carrito; crear pedidos y seguir su estado.

## Cómo iniciarlo

1. Ejecutá [`../api/database/migrate_ecommerce.sql`](../api/database/migrate_ecommerce.sql) en MySQL Workbench. Este único script prepara la base completa, conserva `articulo`, crea las tablas de tienda, migra artículos existentes a `producto` y agrega el usuario demo `demo@mercado.local`.
2. Iniciá la API siguiendo [`../api/README.md`](../api/README.md). Debe responder en `http://localhost:8081`.
3. Abrí `index.html` con Live Server de VS Code.
4. Ingresá con `demo@mercado.local` o creá un usuario desde la pantalla inicial. El acceso de demo no pide contraseña.

## Modelo de datos visto desde el front

El front no mantiene productos ni cantidades de carrito como datos permanentes del navegador: solicita y actualiza los registros mediante la API.

- **Productos (`producto`)**: nombre, descripción, precio, stock, categoría, estado e imagen opcional. Si no hay `imagenUrl` o falla la carga, se muestra un icono. Si se archiva un producto, deja de estar disponible para nuevas compras y se preserva el historial.
- **Categorías (`categoria`)**: agrupan el catálogo; cada producto referencia una categoría.
- **Usuarios (`usuario`)**: el correo identifica a quién pertenece la selección y el historial. La pantalla de ingreso es una identificación sencilla para demo, sin contraseñas ni sesiones seguras.
- **Carrito (`carrito`, `linea_carrito`)**: la API guarda un carrito por usuario y cada fila contiene un producto y una cantidad. Por eso el contenido persiste al salir y volver a ingresar con ese usuario. El precio se obtiene del catálogo vigente.
- **Pedidos (`pedido`, `linea_pedido`)**: crear un pedido toma el contenido del carrito, valida producto y stock y guarda el precio, nombre, cantidad y subtotal de cada línea. El pedido se crea pendiente. Al confirmarlo, vuelve a validar y descuenta stock; si se cancela luego de confirmar, lo reintegra.

El pedido y el vaciado del carrito se ejecutan en una misma transacción en la API. Si falla la validación o el guardado del pedido, la base conserva el carrito para que se pueda corregir y reintentar. El detalle del modelo y las relaciones está en [`../api/README.md`](../api/README.md).

## Limitaciones del acceso demo

La selección de usuario se recuerda en `localStorage` para reabrir la interfaz con la misma cuenta; **Salir** limpia esa selección del navegador, pero no elimina el carrito persistido en MySQL. El login no autentica realmente ni autoriza operaciones en la API; no debe usarse para información real o producción. `root/admin` pertenece a MySQL, no al front.
