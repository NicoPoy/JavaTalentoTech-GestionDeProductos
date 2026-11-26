package com.techlab.servicios;

import com.techlab.excepciones.ProductoNoEncontradoException;
import com.techlab.excepciones.StockInsuficienteException;
import com.techlab.pedidos.LineaPedido;
import com.techlab.pedidos.Pedido;
import com.techlab.productos.Producto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GestionPedidos {
    private final Inventario inventario;
    private final List<Pedido> pedidos = new ArrayList<>();

    public GestionPedidos(Inventario inventario) { this.inventario = inventario; }

    public Pedido crearPedido(Map<Integer, Integer> cantidades) throws ProductoNoEncontradoException, StockInsuficienteException {
        if (cantidades.isEmpty()) throw new IllegalArgumentException("El pedido debe incluir al menos un producto.");

        Map<Producto, Integer> productosValidados = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entrada : cantidades.entrySet()) {
            int cantidad = entrada.getValue();
            if (cantidad <= 0) throw new IllegalArgumentException("Las cantidades deben ser mayores que cero.");
            Producto producto = inventario.buscarPorId(entrada.getKey());
            if (cantidad > producto.getStock()) {
                throw new StockInsuficienteException("Stock insuficiente para '" + producto.getNombre()
                        + "'. Disponible: " + producto.getStock() + ".");
            }
            productosValidados.put(producto, cantidad);
        }

        List<LineaPedido> lineas = new ArrayList<>();
        for (Map.Entry<Producto, Integer> entrada : productosValidados.entrySet()) {
            Producto producto = entrada.getKey();
            int cantidad = entrada.getValue();
            lineas.add(new LineaPedido(producto.getId(), producto.getNombre(), producto.getPrecio(), cantidad));
        }
        for (Map.Entry<Producto, Integer> entrada : productosValidados.entrySet()) {
            entrada.getKey().descontarStock(entrada.getValue());
        }

        Pedido pedido = new Pedido(lineas);
        pedidos.add(pedido);
        return pedido;
    }

    public List<Pedido> listar() { return Collections.unmodifiableList(pedidos); }
}
