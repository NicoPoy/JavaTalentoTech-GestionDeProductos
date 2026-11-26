package com.ejemplo.articulos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LineaCarritoRequest(@NotNull @Positive Long productoId,
        @NotNull @Positive Integer cantidad) {}
