package com.ejemplo.articulos.dto;

import java.math.BigDecimal;

public record LineaCarritoResponse(Long productoId, String nombre, String imagenUrl,
        BigDecimal precioUnitario, Integer cantidad, BigDecimal subtotal, Integer stock) {}
