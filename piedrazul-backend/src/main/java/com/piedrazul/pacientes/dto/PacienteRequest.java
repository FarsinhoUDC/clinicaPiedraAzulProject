package com.piedrazul.pacientes.dto;

import com.piedrazul.pacientes.domain.Genero;
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

    private String correo;

    private String contrasena;

    @NotBlank(message = "El celular es requerido")
    private String celular;

    @NotNull(message = "El genero es requerido")
    private Genero genero;

    private LocalDate fechaNacimiento;
}
