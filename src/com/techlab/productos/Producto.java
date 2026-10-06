package com.techlab.productos;

import java.util.concurrent.atomic.AtomicInteger;

/** Producto del catálogo. Los identificadores se asignan automáticamente. */
public class Producto {
    private static final AtomicInteger PROXIMO_ID = new AtomicInteger(1);

    private final int id;
    private String nombre;
    private double precio;
    private int stock;

    public Producto(String nombre, double precio, int stock) {
        this(PROXIMO_ID.getAndIncrement(), nombre, precio, stock);
    }

    protected Producto(int id, String nombre, double precio, int stock) {
        if (id <= 0) throw new IllegalArgumentException("El ID debe ser positivo.");
        this.id = id;
        setNombre(nombre);
        setPrecio(precio);
        setStock(stock);
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }
    public String getTipo() { return "General"; }

    public void setPrecio(double precio) {
        if (!Double.isFinite(precio) || precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }
        this.precio = precio;
    }

    public void setStock(int stock) {
        if (stock < 0) throw new IllegalArgumentException("El stock no puede ser negativo.");
        this.stock = stock;
    }

    public void descontarStock(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva.");
        if (cantidad > stock) throw new IllegalArgumentException("Stock insuficiente.");
        stock -= cantidad;
    }

    private void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    @Override
    public String toString() {
        return "ID: " + id + " | " + nombre + " | Tipo: " + getTipo()
                + " | Precio: $" + String.format(java.util.Locale.forLanguageTag("es-AR"), "%,.2f", precio)
                + " | Stock: " + stock;
    }
}
