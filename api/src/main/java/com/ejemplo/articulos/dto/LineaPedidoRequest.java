package com.ejemplo.articulos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LineaPedidoRequest(@NotNull Long productoId, @NotNull @Positive Integer cantidad) {}
