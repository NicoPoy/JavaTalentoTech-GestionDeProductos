package com.techlab.app;

import com.techlab.excepciones.ProductoNoEncontradoException;
import com.techlab.excepciones.StockInsuficienteException;
import com.techlab.pedidos.Pedido;
import com.techlab.productos.Bebida;
import com.techlab.productos.Comida;
import com.techlab.productos.Producto;
import com.techlab.servicios.GestionPedidos;
import com.techlab.servicios.Inventario;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private final Scanner entrada = new Scanner(System.in);
    private final Inventario inventario = new Inventario();
    private final GestionPedidos gestionPedidos = new GestionPedidos(inventario);

    public static void main(String[] args) { new Main().iniciar(); }

    private void iniciar() {
        System.out.println("====================================");
        System.out.println("       SISTEMA DE GESTION TECHLAB");
        System.out.println("====================================");
        boolean continuar = true;
        while (continuar) {
            mostrarMenu();
            int opcion = leerEntero("Elija una opcion: ");
            try {
                switch (opcion) {
                    case 1 -> agregarProducto();
                    case 2 -> listarProductos();
                    case 3 -> buscarOActualizarProducto();
                    case 4 -> eliminarProducto();
                    case 5 -> crearPedido();
                    case 6 -> listarPedidos();
                    case 7 -> continuar = false;
                    default -> System.out.println("Opcion invalida. Ingrese un numero del 1 al 7.");
                }
            } catch (ProductoNoEncontradoException | StockInsuficienteException | IllegalArgumentException e) {
                System.out.println("No se pudo completar la operacion: " + e.getMessage());
            }
            System.out.println();
        }
        entrada.close();
        System.out.println("Gracias por usar el sistema. Hasta luego.");
    }

    private void mostrarMenu() {
        System.out.println("1) Agregar producto");
        System.out.println("2) Listar productos");
        System.out.println("3) Buscar/Actualizar producto");
        System.out.println("4) Eliminar producto");
        System.out.println("5) Crear un pedido");
        System.out.println("6) Listar pedidos");
        System.out.println("7) Salir");
    }

    private void agregarProducto() {
        String nombre = leerTexto("Nombre: ");
        double precio = leerDouble("Precio (ejemplo: 150.000,00): ");
        int stock = leerEntero("Cantidad en stock: ");
        System.out.println("Tipo: 1) General  2) Bebida  3) Comida");
        int tipo = leerEntero("Elija el tipo: ");
        Producto producto = switch (tipo) {
            case 1 -> new Producto(nombre, precio, stock);
            case 2 -> new Bebida(nombre, precio, stock);
            case 3 -> new Comida(nombre, precio, stock);
            default -> throw new IllegalArgumentException("El tipo debe ser 1, 2 o 3.");
        };
        inventario.agregar(producto);
        System.out.println("Producto agregado correctamente. ID asignado: " + producto.getId());
    }

    private void listarProductos() {
        List<Producto> productos = inventario.listar();
        if (productos.isEmpty()) { System.out.println("Todavia no hay productos cargados."); return; }
        System.out.println("--- PRODUCTOS ---");
        for (Producto producto : productos) System.out.println(producto);
    }

    private void buscarOActualizarProducto() throws ProductoNoEncontradoException {
        System.out.println("Buscar por: 1) ID  2) Nombre");
        int modo = leerEntero("Elija una opcion: ");
        Producto producto;
        if (modo == 1) {
            producto = inventario.buscarPorId(leerEntero("ID del producto: "));
            System.out.println(producto);
        } else if (modo == 2) {
            List<Producto> encontrados = inventario.buscarPorNombre(leerTexto("Nombre o parte del nombre: "));
            if (encontrados.isEmpty()) throw new ProductoNoEncontradoException("No se encontraron productos con ese nombre.");
            for (Producto encontrado : encontrados) System.out.println(encontrado);
            if (encontrados.size() > 1) {
                int id = leerEntero("Ingrese el ID que desea actualizar (0 para cancelar): ");
                if (id == 0) return;
                producto = inventario.buscarPorId(id);
                if (!encontrados.contains(producto)) {
                    throw new ProductoNoEncontradoException("El ID ingresado no coincide con los resultados de la busqueda.");
                }
            } else producto = encontrados.get(0);
        } else {
            throw new IllegalArgumentException("La opcion debe ser 1 o 2.");
        }
        System.out.println("Actualizar: 1) Precio  2) Stock  3) Volver");
        int campo = leerEntero("Elija una opcion: ");
        if (campo == 1) producto.setPrecio(leerDouble("Nuevo precio: "));
        else if (campo == 2) producto.setStock(leerEntero("Nuevo stock: "));
        else if (campo != 3) throw new IllegalArgumentException("Opcion invalida.");
        if (campo == 1 || campo == 2) System.out.println("Producto actualizado: " + producto);
    }

    private void eliminarProducto() throws ProductoNoEncontradoException {
        int id = leerEntero("ID del producto a eliminar: ");
        Producto producto = inventario.buscarPorId(id);
        System.out.println(producto);
        if (leerTexto("Confirma la eliminacion? (s/n): ").equalsIgnoreCase("s")) {
            inventario.eliminar(id);
            System.out.println("Producto eliminado.");
        } else System.out.println("Eliminacion cancelada.");
    }

    private void crearPedido() throws ProductoNoEncontradoException, StockInsuficienteException {
        if (inventario.listar().isEmpty()) { System.out.println("Agregue productos antes de crear un pedido."); return; }
        listarProductos();
        Map<Integer, Integer> cantidades = new LinkedHashMap<>();
        boolean agregarOtro = true;
        while (agregarOtro) {
            int id = leerEntero("ID del producto (0 para finalizar): ");
            if (id == 0) break;
            int cantidad = leerEntero("Cantidad: ");
            if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
            cantidades.merge(id, cantidad, Integer::sum);
            agregarOtro = leerTexto("Agregar otro producto? (s/n): ").equalsIgnoreCase("s");
        }
        Pedido pedido = gestionPedidos.crearPedido(cantidades);
        System.out.println("Pedido creado correctamente:\n" + pedido);
    }

    private void listarPedidos() {
        List<Pedido> pedidos = gestionPedidos.listar();
        if (pedidos.isEmpty()) { System.out.println("Todavia no hay pedidos realizados."); return; }
        System.out.println("--- PEDIDOS ---");
        for (Pedido pedido : pedidos) System.out.println(pedido + "\n");
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return entrada.nextLine().trim();
    }

    private int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String valor = entrada.nextLine().trim();
            try { return Integer.parseInt(valor); }
            catch (NumberFormatException e) { System.out.println("Ingrese un numero entero valido."); }
        }
    }

    private double leerDouble(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String valor = entrada.nextLine().trim().replace("$", "").replace(" ", "");
            if (valor.contains(",")) {
                valor = valor.replace(".", "").replace(',', '.');
            }
            try { return Double.parseDouble(valor); }
            catch (NumberFormatException e) { System.out.println("Ingrese un precio valido, por ejemplo 150.000,00."); }
        }
    }
}
