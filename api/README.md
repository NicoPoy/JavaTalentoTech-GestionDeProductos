# API REST de artículos

Backend del proyecto integrador: API REST monolítica en capas para crear, consultar, actualizar y eliminar artículos. Usa Java 17, Spring Boot, Spring Data JPA, Hibernate, Maven y MySQL. La interfaz web complementaria está en `../front`.

## Requisitos

- JDK 17.
- MySQL Server iniciado.
- Eclipse IDE for Java Developers con integración Maven (m2e), o Maven 3.8.6 o superior.
- Postman para ejecutar la colección incluida.

## Configurar MySQL

1. Abrí MySQL Workbench y conectate a tu servidor local.
2. Ejecutá una sola vez `database/migrate_ecommerce.sql` en MySQL Workbench. Crea `articulos_db`, todas las tablas y el usuario demo; alcanza para una instalación nueva o para actualizar una existente.
3. La configuración predeterminada usa el usuario `root` y la contraseña `admin`. Si tu instalación tiene otras credenciales, definí `DB_USERNAME` y `DB_PASSWORD` antes de iniciar. También podés cambiar la URL con `DB_URL`.

Para dejar toda la base lista o actualizarla, detené la API y ejecutá todo el archivo `database/migrate_ecommerce.sql` desde MySQL Workbench. Conserva `articulo`, crea las tablas de tienda, copia los artículos al catálogo con stock inicial cero y agrega el usuario demo `demo@mercado.local`. Se puede volver a ejecutar sin borrar los datos.

En PowerShell, por ejemplo:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "tu contraseña de MySQL"
```

Para otros entornos, preferí configurar las credenciales mediante variables de entorno. Si iniciás la aplicación desde Eclipse, cargá las mismas variables en **Run Configurations… → Environment**.

## Abrir y ejecutar en Eclipse

1. Abrí como proyecto la carpeta `api` dentro del repositorio.
2. En Package Explorer, clic derecho sobre el proyecto → **Configure → Convert to Maven Project**. Si Eclipse ya lo reconoce como Maven, usá **Maven → Update Project…**.
3. Esperá a que Maven descargue las dependencias y termine la actualización.
4. Asegurate de que **Project → Properties → Java Build Path → Libraries** use un JDK 17.
5. Confirmá que MySQL esté iniciado y ejecutá `database/migrate_ecommerce.sql`.
6. Clic derecho en `ArticuloApiApplication.java` → **Run As → Java Application**. La API queda disponible en `http://localhost:8081`.

También podés abrir una terminal en la raíz del proyecto y ejecutar:

```powershell
mvn spring-boot:run
```

Para crear un JAR ejecutable:

```powershell
mvn clean package
java -jar target/articulo-api-mysql-0.0.1-SNAPSHOT.jar
```

Si `java -version` muestra una versión distinta, seleccioná Temurin JDK 17 en la configuración de JRE de Eclipse o anteponé la carpeta `bin` del JDK 17 a `PATH` en esa terminal.

## Endpoints

| Método | Ruta | Resultado |
| --- | --- | --- |
| GET | `/api/articulos` | Lista artículos; devuelve `200` y `[]` si no hay registros. |
| GET | `/api/articulos/{id}` | Devuelve el artículo (`200`) o `404`. |
| POST | `/api/articulos` | Crea un artículo y devuelve `200` con el ID asignado. |
| PUT | `/api/articulos/{id}` | Actualiza el artículo (`200`) o devuelve `404`. |
| DELETE | `/api/articulos/{id}` | Elimina y devuelve `204` sin cuerpo, o `404`. |

El JSON de entrada para crear o actualizar es:

```json
{
  "nombre": "Teclado",
  "precio": 15000
}
```

El nombre no puede estar vacío y el precio debe ser mayor que cero. Los datos inválidos producen `400 Bad Request`.

## API adicional de tienda

La consigna PDF exige el endpoint `/api/articulos` con los campos `id`, `nombre` y `precio`. Se mantiene sin cambios para sus pruebas de Postman. La interfaz e-commerce usa además estos endpoints:

| Método | Ruta | Uso |
| --- | --- | --- |
| GET | `/api/productos?incluirInactivos=true` | Lista productos; archivados opcionales. |
| GET | `/api/productos/{id}` | Consulta un producto. |
| GET | `/api/productos/stock-bajo?umbral=5` | Lista productos con stock bajo. |
| POST | `/api/productos` | Crea producto con nombre, descripción, imagenUrl opcional, precio, stock y categoriaId. |
| PUT | `/api/productos/{id}` | Actualiza esos datos. |
| DELETE | `/api/productos/{id}` | Archiva el producto para conservar pedidos históricos. |
| PATCH | `/api/productos/{id}/reactivar` | Reactiva un producto archivado. |
| GET/POST | `/api/categorias` | Lista o crea categorías. |
| PUT/DELETE | `/api/categorias/{id}` | Modifica o elimina categorías sin productos asociados. |
| GET/POST | `/api/usuarios` | Lista o registra usuarios/clientes por correo. |
| PUT/DELETE | `/api/usuarios/{id}` | Modifica o elimina usuarios sin pedidos. |
| POST | `/api/auth/login` | Busca un usuario registrado por correo y lo devuelve para el acceso de demostración. |
| GET/DELETE | `/api/carritos/{usuarioId}` | Consulta o vacía el carrito persistido del usuario. |
| POST | `/api/carritos/{usuarioId}/items` | Agrega producto y cantidad al carrito; suma la cantidad si ya estaba. |
| PUT/DELETE | `/api/carritos/{usuarioId}/items/{productoId}` | Cambia cantidad o quita un producto del carrito. |
| GET | `/api/pedidos?usuarioId={id}` | Lista todos los pedidos o el historial de un usuario. |
| GET | `/api/pedidos/{id}` | Consulta un pedido con sus líneas. |
| POST | `/api/pedidos` | Crea pedido pendiente; body con usuarioId y productos [{ productoId, cantidad }]. |
| POST | `/api/pedidos/desde-carrito` | Valida stock y registra pedido desde el carrito; carrito y pedido se guardan en una transacción. |
| PUT | `/api/pedidos/{id}/estado` | Avanza o cancela el pedido. |

Estados permitidos: `PENDIENTE → CONFIRMADO → ENVIADO → ENTREGADO`; un pedido pendiente o confirmado puede pasar a `CANCELADO`. El stock se valida al crear el pedido, se vuelve a comprobar y se descuenta al confirmar, y se reintegra al cancelar un pedido confirmado. Al crear un pedido desde el carrito, este se vacía solo si la transacción se completa correctamente. Cada producto admite `imagenUrl` HTTP/HTTPS opcional; sin URL, el front muestra un ícono. El login de demostración identifica por correo y mantiene la selección en el navegador; no usa contraseña, token ni protege los endpoints. No es autenticación segura ni debe usarse en producción. La contraseña `root/admin` es únicamente la conexión local a MySQL.

El SQL de migración copia los artículos antiguos a `producto` con stock cero. Editá cada existencia desde el front antes de confirmar pedidos. También agrega `imagen_url`, `carrito` y `linea_carrito`; volvé a ejecutar el archivo de migración completo si ya lo habías aplicado.

## Colección de Postman

Importá `postman/Articulo API MySQL.postman_collection.json` en Postman. La colección usa `http://localhost:8081` como `baseUrl`; ejecutá las primeras diez solicitudes en orden para que las pruebas de actualización y eliminación reutilicen los IDs creados. La solicitud 11 comprueba la lista vacía y requiere una tabla sin registros; la 12 se ejecuta después de reiniciar la aplicación para comprobar persistencia.

Para revisar los cambios directamente en MySQL Workbench:

```sql
USE articulos_db;
DESCRIBE articulo;
SELECT * FROM articulo;
SELECT * FROM articulo WHERE id = 1;
```

## Estructura

```text
src/main/java/com/ejemplo/articulos/
├── ArticuloApiApplication.java
├── controller/ArticuloController.java
├── model/Articulo.java
├── repository/ArticuloRepository.java
└── service/
    ├── ArticuloService.java
    └── ArticuloServiceImpl.java
```

El código de consola de la preentrega anterior se conserva aparte en `preentrega-console/`; no forma parte del source root Maven ni del alcance de esta API.

## Grabación de la defensa

El enunciado pide incluir un enlace a la grabación de la demostración. Después de grabar y subir el video, agregá aquí el enlace de YouTube; todavía no hay una URL de grabación proporcionada.
