# Sistema de gestión TechLab

Esta es mi preentrega de Java para Talento Tech. Armé una aplicación de consola para cargar productos, controlar el stock y registrar pedidos, aplicando los temas que vimos en el curso, como clases, colecciones y excepciones.

## Java utilizado

Desarrollé y compilé el proyecto con **Eclipse Temurin JDK 17.0.20.1+1**. El JDK no está incluido en este repositorio: para ejecutarlo necesitás tener instalado un **JDK 17 o superior** y poder usar `java` y `javac` desde la terminal.

No hacen falta dependencias externas ni una base de datos. Los productos y pedidos quedan en memoria y se reinician al cerrar el programa.

## Cómo ejecutarlo

### Desde IntelliJ IDEA

Abrí esta carpeta en IntelliJ. Si te pide configurar el JDK, elegí **Setup SDK** y seleccioná la carpeta de un JDK 17 o superior. Después abrí `src/com/techlab/app/Main.java` y ejecutá `Main.main()` con el triángulo verde.

Si IntelliJ no reconoce las clases, hacé clic derecho en `src` y elegí **Mark Directory as → Sources Root**.

## Qué se puede hacer

- Agregar productos generales, bebidas y comidas, y consultar el catálogo.
- Buscar productos por ID o nombre, actualizar sus precios o stock y eliminarlos.
- Crear pedidos con varios productos, calcular el total y descontar el stock.
- Consultar los pedidos realizados y sus productos.

Para ingresar un precio podés escribir, por ejemplo, `150000` o `150.000,00`. El sistema muestra los importes con formato argentino, como `$150.000,00`.

## Cómo organicé el código

Las clases están separadas en paquetes: `app` contiene el menú, `productos` el catálogo, `pedidos` los pedidos y sus líneas, `servicios` la lógica de gestión y `excepciones` los errores propios del sistema. También usé herencia y polimorfismo para representar bebidas y comidas como tipos de producto.
