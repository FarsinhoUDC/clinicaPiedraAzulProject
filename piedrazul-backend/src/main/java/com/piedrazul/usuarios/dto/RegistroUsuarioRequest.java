package com.piedrazul.usuarios.dto;

import com.piedrazul.usuarios.domain.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegistroUsuarioRequest {
    @NotBlank(message = "El username es requerido") private String username;
    @NotBlank(message = "La contrasena es requerida") private String password;
    @NotNull(message = "El rol es requerido") private Rol rol;
}
