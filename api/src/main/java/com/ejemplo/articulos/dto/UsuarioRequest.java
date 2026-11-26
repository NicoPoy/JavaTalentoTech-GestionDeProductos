package com.ejemplo.articulos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(@NotBlank @Size(max = 120) String nombre,
        @NotBlank @Email @Size(max = 190) String email) {}
