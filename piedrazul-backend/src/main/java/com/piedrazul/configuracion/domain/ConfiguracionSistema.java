package com.piedrazul.configuracion.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "configuracion_sistema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConfiguracionSistema {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Cuantas semanas hacia adelante se habilitan las citas
    @Column(nullable = false)
    private int ventanaSemanas;
}
