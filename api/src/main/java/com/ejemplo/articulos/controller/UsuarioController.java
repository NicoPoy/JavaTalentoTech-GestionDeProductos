package com.ejemplo.articulos.controller;

import com.ejemplo.articulos.dto.UsuarioRequest;
import com.ejemplo.articulos.dto.UsuarioResponse;
import com.ejemplo.articulos.service.UsuarioService;
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
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {
    private final UsuarioService service;
    public UsuarioController(UsuarioService service) { this.service = service; }
    @GetMapping public List<UsuarioResponse> listar() { return service.listar(); }
    @PostMapping public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) { return ResponseEntity.ok(service.crear(request)); }
    @PutMapping("/{id}") public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) { return service.actualizar(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id) { service.eliminar(id); return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); }
}
