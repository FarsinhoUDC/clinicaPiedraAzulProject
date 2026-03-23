package com.piedrazul.citas.dto;

import com.piedrazul.pacientes.dto.PacienteRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CrearCitaRequest {

    @Valid
    @NotNull(message = "Los datos del paciente son requeridos")
    private PacienteRequest paciente;

    @NotNull(message = "El medico es requerido")
    private Long medicoId;

    @NotNull(message = "La fecha y hora son requeridas")
    @Future(message = "La cita debe ser en el futuro")
    private LocalDateTime fechaHora;
}
