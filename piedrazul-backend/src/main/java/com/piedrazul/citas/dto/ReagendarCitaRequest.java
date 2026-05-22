package com.piedrazul.citas.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReagendarCitaRequest {

    @NotNull(message = "La nueva fecha y hora es requerida")
    @Future(message = "La nueva fecha y hora debe ser en el futuro")
    private LocalDateTime nuevaFechaHora;

    private String motivo;
}
