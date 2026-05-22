package com.piedrazul.citas.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_citas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialCita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    @Column(nullable = false)
    private LocalDateTime fechaAnterior;

    @Column(nullable = false)
    private LocalDateTime fechaNueva;

    @Column(nullable = false)
    private LocalDateTime fechaModificacion;

    @Column(length = 500)
    private String motivo;

    @Column(nullable = false)
    private String usuarioResponsable;

    @PrePersist
    protected void onCreate() {
        this.fechaModificacion = LocalDateTime.now();
    }
}
