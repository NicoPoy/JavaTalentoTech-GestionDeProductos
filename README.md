# JavaTalentoTech - Gestión de productos

Repositorio organizado como un proyecto con backend y frontend:

- [`api/`](api/README.md): API REST en Java 17, Spring Boot, Maven y MySQL. Consultá su README para configurarla y ejecutarla.
- [`front/`](front/README.md): sitio e-commerce estático en HTML, CSS y JavaScript, conectado al catálogo de la API.

Para iniciar el backend desde una terminal abierta en la raíz del repositorio:

```powershell
cd api
mvn spring-boot:run
```

La API escucha en `http://localhost:8081`.

Para abrir el sitio, iniciá el backend y serví `front/index.html` con Live Server de VS Code. El catálogo y el carrito están disponibles; el envío de pedidos todavía está pendiente de integrar con el backend.
