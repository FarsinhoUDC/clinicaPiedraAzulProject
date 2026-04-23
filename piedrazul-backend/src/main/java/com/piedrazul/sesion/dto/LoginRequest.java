package com.piedrazul.sesion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "El número de documento es requerido")
    private String numeroDocumento;

    @NotBlank(message = "La contraseña es requerida")
    private String contrasena;
}
