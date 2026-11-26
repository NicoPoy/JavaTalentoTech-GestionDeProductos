package com.ejemplo.articulos.controller;

import com.ejemplo.articulos.dto.EstadoPedidoRequest;
import com.ejemplo.articulos.dto.PedidoRequest;
import com.ejemplo.articulos.dto.PedidoDesdeCarritoRequest;
import com.ejemplo.articulos.dto.PedidoResponse;
import com.ejemplo.articulos.service.PedidoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {
    private final PedidoService service;
    public PedidoController(PedidoService service) { this.service = service; }

    @GetMapping public List<PedidoResponse> listar(@RequestParam(required = false) Long usuarioId) { return service.listar(usuarioId); }
    @GetMapping("/{id}") public PedidoResponse obtener(@PathVariable Long id) { return service.obtener(id); }
    @PostMapping public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody PedidoRequest request) { return ResponseEntity.ok(service.crear(request)); }
    @PostMapping("/desde-carrito") public ResponseEntity<PedidoResponse> crearDesdeCarrito(@Valid @RequestBody PedidoDesdeCarritoRequest request) { return ResponseEntity.ok(service.crearDesdeCarrito(request.usuarioId())); }
    @PutMapping("/{id}/estado") public PedidoResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoPedidoRequest request) { return service.cambiarEstado(id, request); }
}
