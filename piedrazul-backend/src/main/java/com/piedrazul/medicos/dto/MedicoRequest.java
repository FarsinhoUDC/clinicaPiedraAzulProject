package com.piedrazul.medicos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MedicoRequest {
    @NotBlank(message = "Los nombres son requeridos")
    private String nombres;
    @NotBlank(message = "Los apellidos son requeridos")
    private String apellidos;
    private String especialidad;
}
