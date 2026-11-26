package com.ejemplo.articulos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PedidoRequest(@NotNull Long usuarioId, @NotEmpty List<@Valid LineaPedidoRequest> productos) {}
