package com.piedrazul.pacientes.dto;

import com.piedrazul.pacientes.domain.Genero;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PacienteRequest {

    @NotBlank(message = "El numero de documento es requerido")
    private String numeroDocumento;

    @NotBlank(message = "Los nombres son requeridos")
    private String nombres;

    @NotBlank(message = "Los apellidos son requeridos")
    private String apellidos;

    @NotBlank(message = "El correo es requerido")
    @Email(message = "El correo no tiene un formato válido")
    private String correo;

    @NotBlank(message = "La contraseña es requerida")
    private String contrasena;

    @NotBlank(message = "El celular es requerido")
    private String celular;

    @NotNull(message = "El genero es requerido")
    private Genero genero;

    private LocalDate fechaNacimiento;
}
