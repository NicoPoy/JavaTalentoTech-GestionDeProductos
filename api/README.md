# API REST y base de datos

Backend en capas con Java 17, Spring Boot 3, Spring Data JPA, Hibernate, Maven y MySQL. Mantiene la API de artículos de la consigna inicial y agrega los endpoints de catálogo y tienda consumidos por [`../front`](../front/README.md).

## Requisitos

- JDK 17.
- MySQL Server.
- Visual Studio Code con las extensiones **Extension Pack for Java** y **Spring Boot Extension Pack** (alternativamente, Maven 3.8.6 o superior).
- Postman para importar la colección de `postman/`.

## Instalar o actualizar la base

En MySQL Workbench, abrí y ejecutá el archivo completo [`database/migrate_ecommerce.sql`](database/migrate_ecommerce.sql). Es el único SQL necesario, tanto para una base nueva como para actualizar una existente. Crea `articulos_db`, instala las tablas, agrega una categoría `General` y el usuario de demostración `demo@mercado.local`, y copia los registros de `articulo` a `producto` con stock inicial cero. Se puede volver a ejecutar sin borrar los datos.

La configuración local predeterminada apunta a `localhost:3306`, base `articulos_db`, usuario `root` y contraseña `admin`. Para usar otros valores, configurá `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` como variables de entorno. `root/admin` es solo la conexión a MySQL; no es una cuenta de la aplicación.

## Modelo de datos

Las tablas están separadas para que el inventario, el carrito pendiente y la compra ya registrada tengan reglas y tiempos de vida distintos.

| Tabla | Datos guardados | Motivo y comportamiento |
| --- | --- | --- |
| `articulo` | ID, nombre y precio. | Entidad del ejercicio original. Se mantiene para que `/api/articulos` siga cumpliendo la consigna y sus pruebas. |
| `categoria` | ID y nombre único. | Organiza el catálogo y evita guardar/repetir el nombre de categoría en cada producto. |
| `producto` | Nombre, descripción, `imagen_url` opcional, precio, stock, categoría y `activo`. | Catálogo real de la tienda. El stock disponible se valida al crear y confirmar pedidos. Al quitar un producto se archiva (`activo=false`) para proteger el historial. |
| `usuario` | Nombre y correo único. | Identidad de demostración de la persona que opera la tienda; conecta su carrito e historial. No almacena contraseña. |
| `carrito` | ID y usuario. | Hay como máximo un carrito por usuario; permanece guardado en MySQL entre sesiones del navegador. |
| `linea_carrito` | Carrito, producto y cantidad. | Guarda qué productos y cantidades eligió la persona. El precio no se congela acá: se toma del catálogo al mostrar y al crear el pedido. |
| `pedido` | Usuario, fecha de creación, estado y total. | Registra una compra y su evolución (`PENDIENTE`, `CONFIRMADO`, `ENVIADO`, `ENTREGADO` o `CANCELADO`). |
| `linea_pedido` | Pedido, producto opcional, nombre, cantidad, precio unitario y subtotal. | Guarda una fotografía de los artículos y precios al comprar, para que el historial no cambie cuando se edite o elimine el producto. |

Relaciones principales:

```mermaid
erDiagram
    CATEGORIA ||--o{ PRODUCTO : clasifica
    USUARIO ||--o| CARRITO : conserva
    CARRITO ||--o{ LINEA_CARRITO : contiene
    PRODUCTO ||--o{ LINEA_CARRITO : elegido
    USUARIO ||--o{ PEDIDO : realiza
    PEDIDO ||--|{ LINEA_PEDIDO : incluye
    PRODUCTO o|--o{ LINEA_PEDIDO : referencia
```

`articulo` y `producto` son tablas distintas: el SQL conserva los datos del ejercicio original y los copia al catálogo de tienda. Como `articulo` no tiene categoría ni stock, los registros copiados usan `General` y stock `0`; cargá las existencias reales desde el front antes de confirmar pedidos.

El pedido creado desde el carrito y el vaciado de ese carrito ocurren en una transacción: si no se guarda el pedido (por ejemplo, por stock insuficiente), el carrito permanece. Los pedidos se crean pendientes; el stock se descuenta al confirmarlos y se reintegra si un pedido confirmado se cancela.

El login solo identifica por correo para la demostración; no es autenticación segura y no protege los endpoints. En el front podés usar `demo@mercado.local` o crear otro usuario.

## Ejecutar desde Visual Studio Code

1. Abrí en VS Code la carpeta raíz del repositorio (`JavaTalentoTech-GestionDeProductos`).
2. Instalá **Extension Pack for Java** y **Spring Boot Extension Pack** si todavía no están instalados. Esperá a que la extensión Java detecte `api/pom.xml` e importe Maven y sus dependencias.
3. Seleccioná JDK 17 para el proyecto cuando VS Code lo solicite. Si no lo solicita, abrí la paleta de comandos (`Ctrl+Shift+P`) y usá **Java: Configure Java Runtime** para seleccionar JDK 17.
4. Confirmá que MySQL esté iniciado y ejecutá `database/migrate_ecommerce.sql` desde MySQL Workbench.
5. En el explorador, abrí `api/src/main/java/com/ejemplo/articulos/ArticuloApiApplication.java`. Usá el enlace **Run** que aparece sobre `main` o clic derecho en el archivo → **Run Java**. Esperá en la terminal hasta ver `Started ArticuloApiApplication` y `Tomcat started on port 8081`.

Dejá la ejecución activa mientras usás el front o Postman. No elijas **Run Code** de Code Runner ni compiles esta clase con `javac` directamente: Spring Boot requiere el classpath de Maven y sus dependencias. Si preferís terminal y Maven está instalado, desde la raíz ejecutá `cd api` y `mvn spring-boot:run`. Para generar el JAR, usá `mvn clean package` dentro de `api` y después `java -jar target/articulo-api-mysql-0.0.1-SNAPSHOT.jar`.

## Ingresar al front

El script SQL inserta el usuario de demostración **`demo@mercado.local`** en `usuario`. En la pantalla de inicio del front, escribí ese correo y presioná **Ingresar**; no hay contraseña. `root/admin` son las credenciales de MySQL, no las del login. Si el correo no existe, volvé a ejecutar el SQL completo y reiniciá la API. También se puede crear un usuario desde el enlace **¿Todavía no tenés usuario? Crear uno**.

## Endpoints de artículos originales

| Método | Ruta | Resultado |
| --- | --- | --- |
| GET | `/api/articulos` | Lista artículos. |
| GET | `/api/articulos/{id}` | Consulta uno (`404` si no existe). |
| POST | `/api/articulos` | Crea `{ "nombre": "Teclado", "precio": 15000 }`. |
| PUT | `/api/articulos/{id}` | Actualiza un artículo. |
| DELETE | `/api/articulos/{id}` | Elimina y responde `204`. |

Nombre requerido y precio mayor que cero. Se conserva este contrato para la consigna y la colección de pruebas original.

## Endpoints adicionales de tienda

| Método | Ruta | Uso |
| --- | --- | --- |
| GET | `/api/productos?incluirInactivos=true` | Lista el catálogo; opcionalmente incluye archivados. |
| GET | `/api/productos/{id}` | Consulta producto. |
| GET | `/api/productos/stock-bajo?umbral=5` | Consulta productos con stock bajo. |
| POST / PUT | `/api/productos` / `/api/productos/{id}` | Crea o actualiza; acepta `imagenUrl` HTTP/HTTPS opcional. |
| DELETE / PATCH | `/api/productos/{id}` / `/api/productos/{id}/reactivar` | Archiva o reactiva. |
| GET / POST | `/api/categorias` | Lista o crea categorías. |
| PUT / DELETE | `/api/categorias/{id}` | Actualiza o elimina una categoría sin productos asociados. |
| GET / POST | `/api/usuarios` | Lista o crea usuarios de demostración. |
| PUT / DELETE | `/api/usuarios/{id}` | Actualiza o elimina si no tiene pedidos. |
| POST | `/api/auth/login` | Identifica al usuario por correo. |
| GET / DELETE | `/api/carritos/{usuarioId}` | Consulta o vacía el carrito. |
| POST | `/api/carritos/{usuarioId}/items` | Agrega producto y cantidad (suma si ya existe). |
| PUT / DELETE | `/api/carritos/{usuarioId}/items/{productoId}` | Cambia cantidad o quita el producto. |
| GET | `/api/pedidos?usuarioId={id}` | Consulta pedidos o historial de un usuario. |
| GET | `/api/pedidos/{id}` | Consulta un pedido. |
| POST | `/api/pedidos` | Crea pedido pendiente desde un JSON de líneas. |
| POST | `/api/pedidos/desde-carrito` | Valida stock, crea pedido y vacía el carrito atómicamente. |
| PUT | `/api/pedidos/{id}/estado` | Cambia el estado según las transiciones permitidas. |

## Postman

Importá [`postman/Articulo API MySQL.postman_collection.json`](postman/Articulo%20API%20MySQL.postman_collection.json). La colección usa `http://localhost:8081` como `baseUrl` y tiene dos partes:

- Las solicitudes principales prueban el CRUD de `/api/articulos` requerido por la consigna.
- La carpeta **Tienda - flujo integrado** prueba en orden el login demo, categoría, producto con `imagenUrl`, operaciones de carrito, creación del pedido, vaciado del carrito, descuento y reposición de stock, y archivado del producto.

Antes de ejecutarla, corré `database/migrate_ecommerce.sql`, iniciá la API desde VS Code y verificá que esté activa en el puerto 8081. La carpeta de tienda crea un producto de prueba de forma dinámica y lo archiva al final. La solicitud de persistencia de artículos se puede repetir después de reiniciar la API.

## Código anterior

El proyecto de consola de preentrega se conserva en `preentrega-console/` separado de `src/main/java`; no forma parte del source root de Maven.

## Grabación de la defensa

La consigna pide agregar un enlace a la grabación de la demostración. Cuando el video esté grabado y publicado, colocá aquí su URL; todavía no se incluyó ningún enlace.
