# Sistema de gestion TechLab

Preentrega de Java: aplicación de consola para administrar un catálogo y crear pedidos.

## Requisitos

- JDK 17 o superior.
- No requiere dependencias externas.

## Compilar y ejecutar en Windows (PowerShell)

Desde la carpeta `Proyecto`:

```powershell
$jdkHome = Get-ChildItem 'E:\DESARROLLO\Herramientas\Java\Temurin-17' -Directory | Select-Object -First 1 -ExpandProperty FullName
New-Item -ItemType Directory -Force out | Out-Null
& "$jdkHome\bin\javac.exe" -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object FullName)
& "$jdkHome\bin\java.exe" -cp out com.techlab.app.Main
```

## Funcionalidades

1. Agregar productos con nombre, precio, stock y tipo (general, bebida o comida).
2. Listar los productos con ID, nombre, tipo, precio y stock.
3. Buscar por ID o nombre y actualizar precio o stock.
4. Eliminar productos por ID con confirmación.
5. Crear pedidos con uno o varios productos, calcular el total y descontar stock.
6. Listar pedidos con sus artículos y total.

Los datos se mantienen en memoria mientras la aplicación está abierta.

## Organización

- `com.techlab.app`: menú y entrada/salida por consola.
- `com.techlab.productos`: producto, bebida y comida.
- `com.techlab.pedidos`: pedido y sus líneas.
- `com.techlab.servicios`: inventario y creación de pedidos.
- `com.techlab.excepciones`: excepciones de dominio.

La clase `Producto` encapsula sus atributos; `Bebida` y `Comida` muestran herencia y polimorfismo. La creación de pedidos valida todo el stock antes de descontarlo, de modo que un pedido inválido no descuenta parcialmente el inventario.
