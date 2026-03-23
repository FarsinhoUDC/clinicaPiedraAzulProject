package com.piedrazul.configuracion.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Data
public class DisponibilidadMedicoRequest {

    @NotNull(message = "El medico es requerido")
    private Long medicoId;

    @NotEmpty(message = "Debe seleccionar al menos un dia")
    private Set<DayOfWeek> diasSemana;

    @NotNull(message = "La hora de inicio es requerida")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es requerida")
    private LocalTime horaFin;

    @Min(value = 5, message = "El intervalo minimo es 5 minutos")
    private int intervaloMinutos;
}
