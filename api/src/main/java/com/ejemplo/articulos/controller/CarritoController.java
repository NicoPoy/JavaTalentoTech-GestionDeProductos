package com.ejemplo.articulos.controller;

import com.ejemplo.articulos.dto.CarritoResponse;
import com.ejemplo.articulos.dto.CantidadCarritoRequest;
import com.ejemplo.articulos.dto.LineaCarritoRequest;
import com.ejemplo.articulos.service.CarritoService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/carritos")
@CrossOrigin(origins = "*")
public class CarritoController {
    private final CarritoService service;

    public CarritoController(CarritoService service) { this.service = service; }

    @GetMapping("/{usuarioId}")
    public CarritoResponse obtener(@PathVariable Long usuarioId) { return service.obtener(usuarioId); }

    @PostMapping("/{usuarioId}/items")
    public CarritoResponse agregar(@PathVariable Long usuarioId, @Valid @RequestBody LineaCarritoRequest request) {
        return service.agregar(usuarioId, request);
    }

    @PutMapping("/{usuarioId}/items/{productoId}")
    public CarritoResponse cambiarCantidad(@PathVariable Long usuarioId, @PathVariable Long productoId,
            @Valid @RequestBody CantidadCarritoRequest request) {
        return service.cambiarCantidad(usuarioId, productoId, request.cantidad());
    }

    @DeleteMapping("/{usuarioId}/items/{productoId}")
    public CarritoResponse quitar(@PathVariable Long usuarioId, @PathVariable Long productoId) {
        return service.quitar(usuarioId, productoId);
    }

    @DeleteMapping("/{usuarioId}")
    public CarritoResponse vaciar(@PathVariable Long usuarioId) { return service.vaciar(usuarioId); }
}
