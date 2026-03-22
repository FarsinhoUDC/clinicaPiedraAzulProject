package com.piedrazul.configuracion.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ConfiguracionSistemaRequest {
    @Min(value = 1, message = "La ventana debe ser al menos 1 semana")
    private int ventanaSemanas;
}
