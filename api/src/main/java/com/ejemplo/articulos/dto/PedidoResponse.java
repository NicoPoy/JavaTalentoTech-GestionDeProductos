package com.ejemplo.articulos.dto;

import com.ejemplo.articulos.model.EstadoPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(Long id, Long usuarioId, String usuario, String email,
        LocalDateTime fecha, EstadoPedido estado, BigDecimal total,
        List<LineaPedidoResponse> productos) {}
