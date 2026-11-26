package com.ejemplo.articulos.dto;

import java.math.BigDecimal;
import java.util.List;

public record CarritoResponse(Long usuarioId, List<LineaCarritoResponse> items, BigDecimal total) {}
