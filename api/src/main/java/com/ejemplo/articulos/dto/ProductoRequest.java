package com.ejemplo.articulos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank @Size(max = 255) String nombre,
        @Size(max = 1500) String descripcion,
        @Size(max = 2048) @Pattern(regexp = "(?i)^$|^https?://\\S+$", message = "La imagen debe usar una URL HTTP o HTTPS válida.") String imagenUrl,
        @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal precio,
        @NotNull @Min(0) Integer stock,
        @NotNull Long categoriaId) {}
