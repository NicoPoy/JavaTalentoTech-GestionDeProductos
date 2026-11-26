package com.ejemplo.articulos.service;

import com.ejemplo.articulos.dto.CategoriaRequest;
import com.ejemplo.articulos.dto.CategoriaResponse;
import com.ejemplo.articulos.model.Categoria;
import com.ejemplo.articulos.repository.CategoriaRepository;
import com.ejemplo.articulos.repository.ProductoRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CategoriaService {
    private final CategoriaRepository categorias;
    private final ProductoRepository productos;

    public CategoriaService(CategoriaRepository categorias, ProductoRepository productos) {
        this.categorias = categorias;
        this.productos = productos;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() { return categorias.findAll().stream().map(this::response).toList(); }

    public CategoriaResponse crear(CategoriaRequest request) {
        String nombre = request.nombre().trim();
        if (categorias.findByNombreIgnoreCase(nombre).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre.");
        }
        return response(categorias.save(new Categoria(nombre)));
    }

    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = categorias.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada."));
        String nombre = request.nombre().trim();
        categorias.findByNombreIgnoreCase(nombre).filter(otra -> !otra.getId().equals(id)).ifPresent(otra -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre.");
        });
        categoria.setNombre(nombre);
        return response(categorias.save(categoria));
    }

    public void eliminar(Long id) {
        Categoria categoria = categorias.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada."));
        if (productos.existsByCategoriaId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar: hay productos asociados a esta categoría.");
        }
        try { categorias.delete(categoria); }
        catch (DataIntegrityViolationException ex) { throw new ResponseStatusException(HttpStatus.CONFLICT, "La categoría está en uso."); }
    }

    private CategoriaResponse response(Categoria categoria) { return new CategoriaResponse(categoria.getId(), categoria.getNombre()); }
}
