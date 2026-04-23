package com.piedrazul.sesion.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Entidad base que representa a cualquier usuario del sistema (médico o paciente).
 * Medico y Paciente extienden de esta clase heredando los campos comunes.
 *
 * Campos comunes identificados entre Medico y Paciente:
 *  - nombres
 *  - apellidos
 *
 * Campos propios del módulo sesion (credenciales de acceso):
 *  - correo   → usado como username de login
 *  - contrasena
 *  - rol      → determina el tipo de usuario (MEDICO / PACIENTE)
 *  - activo
 */
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(unique = true)
    private String numeroDocumento;

    @Column(nullable = true)
    private String correo;

    @Column(nullable = true)
    private String contrasena;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolUsuario rol;

    @Column(nullable = false)
    private boolean activo = true;
}
