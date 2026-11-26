package com.ejemplo.articulos.controller;

import com.ejemplo.articulos.dto.LoginRequest;
import com.ejemplo.articulos.dto.UsuarioResponse;
import com.ejemplo.articulos.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final UsuarioService service;

    public AuthController(UsuarioService service) { this.service = service; }

    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request.email());
    }
}
