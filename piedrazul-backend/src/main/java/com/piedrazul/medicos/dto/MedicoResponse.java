package com.piedrazul.medicos.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MedicoResponse {

    private Long id;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String correo;
    private String especialidad;
    private boolean activo;
    private String descripcion;
    private String anosExperiencia;
    private String fotoUrl;
}
