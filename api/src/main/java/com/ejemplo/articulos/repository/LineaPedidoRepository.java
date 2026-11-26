package com.ejemplo.articulos.repository;

import com.ejemplo.articulos.model.LineaPedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineaPedidoRepository extends JpaRepository<LineaPedido, Long> {
    boolean existsByProductoId(Long productoId);
}
