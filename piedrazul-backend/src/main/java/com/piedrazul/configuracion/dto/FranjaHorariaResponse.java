package com.piedrazul.configuracion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;

@Data
@AllArgsConstructor
public class FranjaHorariaResponse {
    private LocalTime hora;
    private boolean disponible;
}
