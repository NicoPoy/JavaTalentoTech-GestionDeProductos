package com.techlab.pedidos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pedido {
    private static int proximoId = 1;
    private final int id;
    private final LocalDateTime fecha;
    private final List<LineaPedido> lineas;

    public Pedido(List<LineaPedido> lineas) {
        this.id = proximoId++;
        this.fecha = LocalDateTime.now();
        this.lineas = Collections.unmodifiableList(new ArrayList<>(lineas));
    }

    public int getId() { return id; }
    public List<LineaPedido> getLineas() { return lineas; }

    public double calcularTotal() {
        double total = 0;
        for (LineaPedido linea : lineas) total += linea.getSubtotal();
        return total;
    }

    @Override
    public String toString() {
        StringBuilder salida = new StringBuilder("Pedido #" + id + " | "
                + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n");
        for (LineaPedido linea : lineas) salida.append("  - ").append(linea).append("\n");
        salida.append("  Total: $").append(String.format(java.util.Locale.forLanguageTag("es-AR"), "%,.2f", calcularTotal()));
        return salida.toString();
    }
}
