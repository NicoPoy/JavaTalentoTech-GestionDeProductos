package com.ejemplo.articulos.dto;

import java.math.BigDecimal;

public record LineaPedidoResponse(Long productoId, String producto, Integer cantidad,
        BigDecimal precioUnitario, BigDecimal subtotal) {}
