package com.ejemplo.articulos.repository;

import com.ejemplo.articulos.model.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findAllByActivoTrueOrderByIdDesc();
    List<Producto> findAllByOrderByIdDesc();
    List<Producto> findAllByActivoTrueAndStockLessThanEqualOrderByStockAsc(Integer stock);
    boolean existsByCategoriaId(Long categoriaId);
    @Modifying(flushAutomatically = true)
    @Query("update Producto p set p.stock = p.stock - :cantidad where p.id = :id and p.stock >= :cantidad")
    int descontarStock(@Param("id") Long id, @Param("cantidad") Integer cantidad);

    @Modifying(flushAutomatically = true)
    @Query("update Producto p set p.stock = p.stock + :cantidad where p.id = :id")
    int reponerStock(@Param("id") Long id, @Param("cantidad") Integer cantidad);
}
