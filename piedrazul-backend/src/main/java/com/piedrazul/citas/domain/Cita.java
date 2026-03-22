package com.piedrazul.citas.domain;

import com.piedrazul.medicos.domain.Medico;
import com.piedrazul.pacientes.domain.Paciente;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "citas",
    uniqueConstraints = @UniqueConstraint(columnNames = {"medico_id","fecha_hora"},
        name = "uk_cita_medico_hora"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cita {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @Column(nullable = false) private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) private OrigenCita origen;

    @Column(nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    protected void onCreate() { this.creadoEn = LocalDateTime.now(); }
}
