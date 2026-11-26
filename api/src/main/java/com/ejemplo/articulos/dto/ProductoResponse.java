package com.ejemplo.articulos.dto;

import java.math.BigDecimal;

public record ProductoResponse(Long id, String nombre, String descripcion, String imagenUrl, BigDecimal precio,
        Integer stock, Long categoriaId, String categoria, Boolean activo) {}
