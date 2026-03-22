package com.piedrazul.configuracion.domain;

import com.piedrazul.medicos.domain.Medico;
import jakarta.persistence.*;
import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Entity @Table(name = "disponibilidad_medico")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DisponibilidadMedico {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    // Dias de la semana que atiende (ej: MONDAY, WEDNESDAY, FRIDAY)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "disponibilidad_dias", joinColumns = @JoinColumn(name = "disponibilidad_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana")
    private Set<DayOfWeek> diasSemana;

    @Column(nullable = false) private LocalTime horaInicio;
    @Column(nullable = false) private LocalTime horaFin;

    // Intervalo entre citas en minutos (ej: 30)
    @Column(nullable = false) private int intervaloMinutos;
}
