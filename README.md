# JavaTalentoTech — Gestión de productos y tienda

## Presentación

Hola, soy **Nicolás Poy Peters**, Analista Programador Universitario egresado de la Facultad de Informática de la Universidad Nacional de La Plata. Continúo ampliando mi formación en desarrollo web y backend, con interés en crear aplicaciones útiles y entender cómo se conectan sus distintas partes. Este repositorio contiene uno de mis proyectos integradores de Talento Tech y forma parte de mi recorrido como desarrollador. Podés conocer más sobre mi perfil profesional en [LinkedIn](https://www.linkedin.com/in/poynicolas/).

### Formación

- **Analista Programador Universitario** — Facultad de Informática, Universidad Nacional de La Plata (2023).
- **React JS** — Talento Tech, curso de 80 horas (2026).

## Sobre el proyecto

Proyecto integrador con una API REST en Java 17, Spring Boot y MySQL, y un front responsive en HTML, CSS y JavaScript. El panel permite administrar productos, categorías y usuarios; conservar un carrito por usuario; y crear y gestionar pedidos.

## Estructura

- [`api/`](api/README.md): backend, migración SQL y colección Postman.
- [`front/`](front/README.md): interfaz dark mode para gestionar la tienda.

## Inicio rápido

1. En MySQL Workbench, ejecutá el script completo [`api/database/migrate_ecommerce.sql`](api/database/migrate_ecommerce.sql). Crea o actualiza `articulos_db` y agrega el usuario demo.
2. Abrí la carpeta raíz del repositorio en **Visual Studio Code**. Instalá las extensiones **Extension Pack for Java** y **Spring Boot Extension Pack**, y esperá que VS Code importe el proyecto Maven.
3. En el explorador de VS Code, abrí `api/src/main/java/com/ejemplo/articulos/ArticuloApiApplication.java` y hacé clic en **Run** arriba del método `main`. La API queda en `http://localhost:8081`; dejá esa ejecución activa.
4. Abrí `front/index.html` y seleccioná **Go Live** (Live Server). El navegador abre el front en `http://127.0.0.1:5500`.
5. En la pantalla de acceso, ingresá **`demo@mercado.local`** y presioná **Ingresar**. **No lleva contraseña.**

El SQL crea `demo@mercado.local` en la tabla `usuario`. Si el front no acepta ese correo, verificá que ejecutaste el script en MySQL y que la API sigue activa. `root/admin` corresponde únicamente a la conexión de MySQL Workbench; no es el usuario de la tienda.

## Modelo de datos

La base separa el catálogo, la selección temporal de compra y los pedidos confirmados para conservar la información adecuada en cada etapa:

| Tabla | Qué guarda | Por qué existe |
| --- | --- | --- |
| `articulo` | `id`, `nombre`, `precio` del ejercicio inicial. | Conserva el endpoint `/api/articulos` solicitado por la consigna original. |
| `categoria` | Nombres únicos de categorías. | Permite clasificar productos sin repetir el texto de categoría en cada artículo. |
| `producto` | Nombre, descripción, URL de imagen opcional, precio, stock, categoría y estado activo. | Es el catálogo que usa el front y la fuente de inventario para validar pedidos. |
| `usuario` | Nombre y correo único. | Identifica al usuario activo y vincula su carrito y sus pedidos. |
| `carrito` | Un registro por usuario. | Mantiene la selección en MySQL aunque se cierre sesión o el navegador. |
| `linea_carrito` | Producto y cantidad elegida en cada carrito. | Guarda los ítems del carrito; el precio se consulta del catálogo para mostrar el total vigente. |
| `pedido` | Usuario, fecha, estado y total al realizar la compra. | Registra la compra y su ciclo de vida. |
| `linea_pedido` | Nombre, cantidad, precio unitario y subtotal de cada producto al comprar. | Conserva una fotografía histórica de lo vendido aunque luego cambien los productos o precios. |

Relaciones: cada categoría tiene muchos productos; cada usuario tiene como máximo un carrito y puede tener muchos pedidos; carrito y pedido tienen varias líneas; cada línea apunta a un producto. En las líneas de pedido se conserva además el nombre y el precio de ese momento. Los artículos antiguos se copian a `producto` con stock `0`, para que se cargue la existencia real antes de confirmar ventas.

Para conocer los endpoints, la configuración de ejecución y las pruebas, consultá [`api/README.md`](api/README.md) y [`front/README.md`](front/README.md).
