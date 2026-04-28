package com.piedrazul.medicos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MedicoRequest {

    @NotBlank(message = "Los nombres son requeridos")
    private String nombres;

    @NotBlank(message = "Los apellidos son requeridos")
    private String apellidos;

    @NotBlank(message = "El numero de documento es requerido")
    private String numeroDocumento;

    private String correo;

    private String celular;

    private String genero;

    private java.time.LocalDate fechaNacimiento;

    private String especialidad;
}
