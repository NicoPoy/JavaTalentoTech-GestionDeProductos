package com.techlab.productos;

public class Bebida extends Producto {
    public Bebida(String nombre, double precio, int stock) {
        super(nombre, precio, stock);
    }

    @Override
    public String getTipo() { return "Bebida"; }
}
