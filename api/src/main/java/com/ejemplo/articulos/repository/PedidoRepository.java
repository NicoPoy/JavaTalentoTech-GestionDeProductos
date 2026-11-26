package com.ejemplo.articulos.repository;

import com.ejemplo.articulos.model.Pedido;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    boolean existsByUsuarioId(Long usuarioId);
    @Query("select distinct p from Pedido p join fetch p.usuario left join fetch p.lineas l left join fetch l.producto pr left join fetch pr.categoria order by p.fechaCreacion desc")
    List<Pedido> findAllWithDetails();

    @Query("select distinct p from Pedido p join fetch p.usuario left join fetch p.lineas l left join fetch l.producto pr left join fetch pr.categoria where p.id = :id")
    Optional<Pedido> findByIdWithDetails(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select distinct p from Pedido p join fetch p.usuario left join fetch p.lineas l left join fetch l.producto pr left join fetch pr.categoria where p.id = :id")
    Optional<Pedido> findByIdForUpdateWithDetails(@Param("id") Long id);

    @Query("select distinct p from Pedido p join fetch p.usuario left join fetch p.lineas l left join fetch l.producto pr left join fetch pr.categoria where p.usuario.id = :userId order by p.fechaCreacion desc")
    List<Pedido> findByUserIdWithDetails(@Param("userId") Long userId);
}
