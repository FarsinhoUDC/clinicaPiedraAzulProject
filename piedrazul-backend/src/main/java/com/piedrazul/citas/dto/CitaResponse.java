package com.piedrazul.citas.dto;

import com.piedrazul.citas.domain.OrigenCita;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CitaResponse {
    private Long id;
    private Long pacienteId;
    private String nombrePaciente;
    private String documentoPaciente;
    private String celularPaciente;
    private Long medicoId;
    private String nombreMedico;
    private LocalDateTime fechaHora;
    private OrigenCita origen;
    private LocalDateTime creadoEn;
}
