# Frontend de gestión de tienda

Panel responsive en HTML, CSS y JavaScript, con tema oscuro e iconos. Permite gestionar productos, categorías, usuarios y stock; buscar en el catálogo; guardar productos en el carrito; crear pedidos y seguir su estado.

## Cómo iniciarlo

1. Ejecutá [`../api/database/migrate_ecommerce.sql`](../api/database/migrate_ecommerce.sql) completo en MySQL Workbench. Crea las tablas y el usuario demo `demo@mercado.local`.
2. En VS Code, iniciá la API desde `api/src/main/java/com/ejemplo/articulos/ArticuloApiApplication.java` usando **Run** sobre `main`. Seguí [`../api/README.md`](../api/README.md) si hace falta preparar las extensiones Java. La API debe estar activa en `http://localhost:8081`.
3. Abrí `front/index.html` en VS Code y presioná **Go Live** para servirlo con Live Server.
4. En el navegador, ingresá **`demo@mercado.local`** y presioná **Ingresar**. No hay contraseña. Si no funciona, verificá que ejecutaste el SQL y que la API sigue activa.

`root/admin` es el usuario y contraseña predeterminados de MySQL; no se ingresan en el login del front. También podés desplegar el formulario **¿Todavía no tenés usuario? Crear uno** y registrar un correo nuevo.

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
