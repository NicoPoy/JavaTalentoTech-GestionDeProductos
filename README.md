# NICOLAS POY PETERS - PRE ENTREGA - JAVA 26223

# Sistema de gestión TechLab

Esta es mi preentrega de Java para Talento Tech. Armé una aplicación de consola para cargar productos, controlar el stock y registrar pedidos, aplicando los temas que vimos en el curso, como clases, colecciones y excepciones.

## Java utilizado

Desarrollé y compilé el proyecto con **Eclipse Temurin JDK 17.0.20.1+1**. El JDK no está incluido en este repositorio: para ejecutarlo necesitás tener instalado un **JDK 17 o superior** y poder usar `java` y `javac` desde la terminal.

## Qué se puede hacer

- Agregar productos generales, bebidas y comidas, y consultar el catálogo.
- Buscar productos por ID o nombre, actualizar sus precios o stock y eliminarlos.
- Crear pedidos con varios productos, calcular el total y descontar el stock.
- Consultar los pedidos realizados y sus productos.

## Cómo organicé el código

Las clases están separadas en paquetes: `app` contiene el menú, `productos` el catálogo, `pedidos` los pedidos y sus líneas, `servicios` la lógica de gestión y `excepciones` los errores propios del sistema. También usé herencia y polimorfismo para representar bebidas y comidas como tipos de producto.
