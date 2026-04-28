package com.piedrazul.medicos.domain;

import com.piedrazul.sesion.domain.RolUsuario;
import com.piedrazul.sesion.domain.Usuario;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Médico del sistema. Extiende de Usuario heredando:
 *  id, nombres, apellidos, correo, contrasena, rol, activo.
 *
 * Campos propios del médico:
 *  - especialidad
 */
@Entity
@Table(name = "medicos")
@PrimaryKeyJoinColumn(name = "usuario_id")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Medico extends Usuario {

    private String especialidad;
    
    @Column(nullable = true)
    private String celular;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private com.piedrazul.pacientes.domain.Genero genero;

    private java.time.LocalDate fechaNacimiento;

    public static Medico nuevo(String nombres, String apellidos,
                                String correo, String contrasena,
                                String numeroDocumento,
                                String especialidad,
                                String celular,
                                com.piedrazul.pacientes.domain.Genero genero,
                                java.time.LocalDate fechaNacimiento) {
        Medico m = new Medico();
        m.setNombres(nombres);
        m.setApellidos(apellidos);
        m.setCorreo(correo);
        m.setContrasena(contrasena);
        m.setNumeroDocumento(numeroDocumento);
        m.setRol(RolUsuario.MEDICO);
        m.setActivo(true);
        m.setEspecialidad(especialidad);
        m.setCelular(celular != null ? celular : "");
        m.setGenero(genero);
        m.setFechaNacimiento(fechaNacimiento);
        return m;
    }
}
