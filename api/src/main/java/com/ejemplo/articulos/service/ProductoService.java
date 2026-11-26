package com.ejemplo.articulos.service;

import com.ejemplo.articulos.dto.ProductoRequest;
import com.ejemplo.articulos.dto.ProductoResponse;
import com.ejemplo.articulos.model.Categoria;
import com.ejemplo.articulos.model.Producto;
import com.ejemplo.articulos.repository.CategoriaRepository;
import com.ejemplo.articulos.repository.ProductoRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ProductoService {
    private final ProductoRepository productos;
    private final CategoriaRepository categorias;

    public ProductoService(ProductoRepository productos, CategoriaRepository categorias) {
        this.productos = productos;
        this.categorias = categorias;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(boolean incluirInactivos) {
        List<Producto> lista = incluirInactivos ? productos.findAllByOrderByIdDesc() : productos.findAllByActivoTrueOrderByIdDesc();
        return lista.stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> stockBajo(int umbral) {
        return productos.findAllByActivoTrueAndStockLessThanEqualOrderByStockAsc(umbral).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        Producto producto = productos.findById(id).orElseThrow(() -> notFound("Producto", id));
        return response(producto);
    }

    public ProductoResponse crear(ProductoRequest request) {
        Producto producto = new Producto();
        aplicar(producto, request);
        producto.setActivo(true);
        return response(productos.save(producto));
    }

    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = productos.findById(id).orElseThrow(() -> notFound("Producto", id));
        aplicar(producto, request);
        return response(productos.save(producto));
    }

    public void archivar(Long id) {
        Producto producto = productos.findById(id).orElseThrow(() -> notFound("Producto", id));
        producto.setActivo(false);
        productos.save(producto);
    }

    public ProductoResponse reactivar(Long id) {
        Producto producto = productos.findById(id).orElseThrow(() -> notFound("Producto", id));
        producto.setActivo(true);
        return response(productos.save(producto));
    }

    private void aplicar(Producto producto, ProductoRequest request) {
        Categoria categoria = categorias.findById(request.categoriaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoría indicada no existe."));
        producto.setNombre(request.nombre().trim());
        producto.setDescripcion(request.descripcion() == null ? "" : request.descripcion().trim());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setCategoria(categoria);
    }

    private ProductoResponse response(Producto producto) {
        return new ProductoResponse(producto.getId(), producto.getNombre(), producto.getDescripcion(),
                producto.getPrecio(), producto.getStock(), producto.getCategoria().getId(),
                producto.getCategoria().getNombre(), producto.getActivo());
    }

    private ResponseStatusException notFound(String tipo, Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, tipo + " " + id + " no encontrado.");
    }
}
