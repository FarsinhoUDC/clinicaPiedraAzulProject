package com.piedrazul.configuracion.dto;

import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Data
@Builder
public class DisponibilidadMedicoResponse {
    private Long id;
    private Long medicoId;
    private String nombreMedico;
    private Set<DayOfWeek> diasSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int intervaloMinutos;
}
