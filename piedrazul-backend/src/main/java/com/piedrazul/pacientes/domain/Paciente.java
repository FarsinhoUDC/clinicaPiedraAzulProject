package com.piedrazul.pacientes.domain;

import com.piedrazul.sesion.domain.RolUsuario;
import com.piedrazul.sesion.domain.Usuario;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Paciente del sistema. Extiende de Usuario heredando:
 *  id, nombres, apellidos, correo, contrasena, rol, activo.
 *
 * Campos propios del paciente:
 *  - numeroDocumento
 *  - celular
 *  - genero
 *  - fechaNacimiento
 */
@Entity
@Table(name = "pacientes")
@PrimaryKeyJoinColumn(name = "usuario_id")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Paciente extends Usuario {

    /**
     * Con Keycloak, los pacientes se crean automáticamente al agendar su primera cita.
     * El celular y género son opcionales — Keycloak solo provee nombre y documento.
     */
    @Column(nullable = true)
    private String celular;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Genero genero;

    private LocalDate fechaNacimiento;

    /**
     * Factory method de conveniencia para crear un Paciente con rol ya asignado.
     */
    public static Paciente nuevo(String nombres, String apellidos,
                                  String correo, String contrasena,
                                  String numeroDocumento, String celular,
                                  Genero genero, LocalDate fechaNacimiento) {
        Paciente p = new Paciente();
        p.setNombres(nombres);
        p.setApellidos(apellidos);
        p.setCorreo(correo);
        p.setContrasena(contrasena);
        p.setRol(RolUsuario.PACIENTE);
        p.setActivo(true);
        p.setNumeroDocumento(numeroDocumento);
        // Valores opcionales — pueden ser null cuando el registro viene de Keycloak
        p.setCelular(celular != null ? celular : "");
        p.setGenero(genero); // null permitido en la BD
        p.setFechaNacimiento(fechaNacimiento);
        return p;
    }
}
