package com.techlab.productos;

public class Comida extends Producto {
    public Comida(String nombre, double precio, int stock) {
        super(nombre, precio, stock);
    }

    @Override
    public String getTipo() { return "Comida"; }
}
