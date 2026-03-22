package com.piedrazul.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "El username es requerido") private String username;
    @NotBlank(message = "La contrasena es requerida") private String password;
}
