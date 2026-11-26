package com.ejemplo.articulos.controller;

import com.ejemplo.articulos.dto.CategoriaRequest;
import com.ejemplo.articulos.dto.CategoriaResponse;
import com.ejemplo.articulos.service.CategoriaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {
    private final CategoriaService service;
    public CategoriaController(CategoriaService service) { this.service = service; }
    @GetMapping public List<CategoriaResponse> listar() { return service.listar(); }
    @PostMapping public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest request) { return ResponseEntity.ok(service.crear(request)); }
    @PutMapping("/{id}") public CategoriaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) { return service.actualizar(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id) { service.eliminar(id); return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); }
}
