package com.ejemplo.articulos.dto;

import jakarta.validation.constraints.NotNull;

public record PedidoDesdeCarritoRequest(@NotNull Long usuarioId) {}
