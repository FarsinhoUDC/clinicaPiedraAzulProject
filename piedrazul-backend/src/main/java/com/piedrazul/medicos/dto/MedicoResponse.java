package com.piedrazul.medicos.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MedicoResponse {
    private Long id;
    private String nombres;
    private String apellidos;
    private String especialidad;
    private boolean activo;
}
