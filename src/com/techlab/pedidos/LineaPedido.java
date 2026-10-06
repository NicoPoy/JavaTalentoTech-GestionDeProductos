package com.techlab.pedidos;

public class LineaPedido {
    private final int productoId;
    private final String nombreProducto;
    private final double precioUnitario;
    private final int cantidad;

    public LineaPedido(int productoId, String nombreProducto, double precioUnitario, int cantidad) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    public int getProductoId() { return productoId; }
    public String getNombreProducto() { return nombreProducto; }
    public double getPrecioUnitario() { return precioUnitario; }
    public int getCantidad() { return cantidad; }
    public double getSubtotal() { return precioUnitario * cantidad; }

    @Override
    public String toString() {
        return nombreProducto + " (ID " + productoId + ") x" + cantidad
                + " | $" + String.format(java.util.Locale.forLanguageTag("es-AR"), "%,.2f", getSubtotal());
    }
}
