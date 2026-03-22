package com.piedrazul.pacientes.domain;

import com.piedrazul.usuarios.domain.Usuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "pacientes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Paciente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true) private String numeroDocumento;
    @Column(nullable = false) private String nombres;
    @Column(nullable = false) private String apellidos;
    @Column(nullable = false) private String celular;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Genero genero;
    private LocalDate fechaNacimiento;
    private String correo;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
