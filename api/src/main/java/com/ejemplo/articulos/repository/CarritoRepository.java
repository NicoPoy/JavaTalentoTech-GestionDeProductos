package com.ejemplo.articulos.repository;

import com.ejemplo.articulos.model.Carrito;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    @Query("select distinct c from Carrito c join fetch c.usuario left join fetch c.lineas l left join fetch l.producto p left join fetch p.categoria where c.usuario.id = :usuarioId")
    Optional<Carrito> findByUsuarioIdWithDetails(@Param("usuarioId") Long usuarioId);
}
