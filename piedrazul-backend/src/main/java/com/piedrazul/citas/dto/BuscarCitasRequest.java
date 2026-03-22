package com.piedrazul.citas.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
public class BuscarCitasRequest {
    @NotNull(message = "El medico es requerido") private Long medicoId;
    @NotNull(message = "La fecha es requerida")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;
}
