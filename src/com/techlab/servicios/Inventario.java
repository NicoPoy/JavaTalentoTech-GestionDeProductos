package com.techlab.servicios;

import com.techlab.excepciones.ProductoNoEncontradoException;
import com.techlab.productos.Producto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Inventario {
    private final List<Producto> productos = new ArrayList<>();

    public void agregar(Producto producto) { productos.add(producto); }

    public List<Producto> listar() { return Collections.unmodifiableList(productos); }

    public Producto buscarPorId(int id) throws ProductoNoEncontradoException {
        for (Producto producto : productos) {
            if (producto.getId() == id) return producto;
        }
        throw new ProductoNoEncontradoException("No existe un producto con ID " + id + ".");
    }

    public List<Producto> buscarPorNombre(String nombre) {
        List<Producto> encontrados = new ArrayList<>();
        String consulta = nombre.trim().toLowerCase(java.util.Locale.ROOT);
        for (Producto producto : productos) {
            if (producto.getNombre().toLowerCase(java.util.Locale.ROOT).contains(consulta)) encontrados.add(producto);
        }
        return encontrados;
    }

    public void eliminar(int id) throws ProductoNoEncontradoException {
        productos.remove(buscarPorId(id));
    }
}
