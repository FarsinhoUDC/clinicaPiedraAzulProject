package com.piedrazul.agendadores.dto;

import com.piedrazul.pacientes.domain.Genero;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AgendadorRequest {

    @NotBlank(message = "El numero de documento es requerido")
    private String numeroDocumento;

    @NotBlank(message = "Los nombres son requeridos")
    private String nombres;

    @NotBlank(message = "Los apellidos son requeridos")
    private String apellidos;

    private String correo;

    private String celular;

    private Genero genero;

    private LocalDate fechaNacimiento;
}
