# API REST de artículos

Backend del proyecto integrador: API REST monolítica en capas para crear, consultar, actualizar y eliminar artículos. Usa Java 17, Spring Boot, Spring Data JPA, Hibernate, Maven y MySQL. La interfaz web complementaria está en `../front`.

## Requisitos

- JDK 17.
- MySQL Server iniciado.
- Eclipse IDE for Java Developers con integración Maven (m2e), o Maven 3.8.6 o superior.
- Postman para ejecutar la colección incluida.

## Configurar MySQL

1. Abrí MySQL Workbench y conectate a tu servidor local.
2. Ejecutá el contenido de `database/schema.sql`. Esto crea la base `articulos_db` y, si todavía no existe, la tabla `articulo`.
3. La configuración predeterminada usa el usuario `root` y la contraseña `admin`. Si tu instalación tiene otras credenciales, definí `DB_USERNAME` y `DB_PASSWORD` antes de iniciar. También podés cambiar la URL con `DB_URL`.

En PowerShell, por ejemplo:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "tu contraseña de MySQL"
```

Para otros entornos, preferí configurar las credenciales mediante variables de entorno. Si iniciás la aplicación desde Eclipse, cargá las mismas variables en **Run Configurations… → Environment**.

## Abrir y ejecutar en Eclipse

1. Abrí como proyecto la carpeta `api` dentro del repositorio `JavaTalentoTech-GestionDeProductos`.
2. En Package Explorer, clic derecho sobre el proyecto → **Configure → Convert to Maven Project**. Si Eclipse ya lo reconoce como Maven, usá **Maven → Update Project…**.
3. Esperá a que Maven descargue las dependencias y termine la actualización.
4. Asegurate de que **Project → Properties → Java Build Path → Libraries** use un JDK 17.
5. Confirmá que MySQL esté iniciado y que `articulos_db` exista.
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
