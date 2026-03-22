package com.piedrazul.medicos.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "medicos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Medico {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String nombres;
    @Column(nullable = false) private String apellidos;
    private String especialidad;
    @Column(nullable = false) @Builder.Default private boolean activo = true;
}
