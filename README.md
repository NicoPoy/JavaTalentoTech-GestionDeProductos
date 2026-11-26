# JavaTalentoTech - Gestión de productos

Repositorio organizado como un proyecto con backend y frontend:

- [`api/`](api/README.md): API REST en Java 17, Spring Boot, Maven y MySQL. Consultá su README para configurarla y ejecutarla.
- [`front/`](front/README.md): panel de tienda responsive en dark mode para catálogo, categorías, usuarios, stock, carrito y pedidos.

Para iniciar el backend desde una terminal abierta en la raíz del repositorio:

```powershell
cd api
mvn spring-boot:run
```

La API escucha en `http://localhost:8081`.

Para habilitar las tablas de tienda existentes, ejecutá `api/database/migrate_ecommerce.sql` en MySQL Workbench. Iniciá el backend y serví `front/index.html` con Live Server de VS Code. `/api/articulos` se conserva para las pruebas originales del PDF; la interfaz utiliza `/api/productos`, `/api/categorias`, `/api/usuarios` y `/api/pedidos`.
