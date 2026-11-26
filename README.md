# JavaTalentoTech — Gestión de productos y tienda

## Presentación

Hola, soy **Nicolás Poy Peters**. Estoy formándome en desarrollo de software y actualmente profundizo mis conocimientos de Java, Spring Boot, bases de datos y desarrollo web. En lo profesional, estoy orientando mi perfil a crear aplicaciones útiles, comprender cómo se conectan sus distintas partes y seguir creciendo como desarrollador. Este repositorio reúne uno de mis proyectos integradores de Talento Tech y refleja ese recorrido de aprendizaje.

## Sobre el proyecto

Proyecto integrador con una API REST en Java 17, Spring Boot y MySQL, y un front responsive en HTML, CSS y JavaScript. El panel permite administrar productos, categorías y usuarios; conservar un carrito por usuario; y crear y gestionar pedidos.

## Estructura

- [`api/`](api/README.md): backend, migración SQL y colección Postman.
- [`front/`](front/README.md): interfaz dark mode para gestionar la tienda.

## Inicio rápido

1. Ejecutá una sola vez [`api/database/migrate_ecommerce.sql`](api/database/migrate_ecommerce.sql) en MySQL Workbench. El script instala o actualiza la base `articulos_db` sin borrar los datos existentes.
2. Iniciá MySQL y la API con JDK 17 desde Eclipse o, si Maven está disponible, ejecutá `cd api` y `mvn spring-boot:run`.
3. Abrí `front/index.html` con Live Server de VS Code. La API escucha en `http://localhost:8081`.
4. Ingresá en el front con el correo de un usuario existente, por ejemplo `demo@mercado.local`, creado por el SQL.

Las credenciales `root/admin` son únicamente la conexión local a MySQL. El acceso del front es una identificación de demostración por correo, sin contraseña ni protección de endpoints.

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
