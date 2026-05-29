package com.piedrazul.agendadores.dto;

import com.piedrazul.pacientes.domain.Genero;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AgendadorResponse {

    private Long id;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String correo;
    private String celular;
    private Genero genero;
    private LocalDate fechaNacimiento;
}
