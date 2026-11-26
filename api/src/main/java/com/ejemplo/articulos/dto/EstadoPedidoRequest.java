package com.ejemplo.articulos.dto;

import com.ejemplo.articulos.model.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record EstadoPedidoRequest(@NotNull EstadoPedido estado) {}
