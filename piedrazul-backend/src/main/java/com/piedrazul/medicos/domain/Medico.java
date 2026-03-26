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

    /**
     * Factory method de conveniencia para crear un Medico con rol ya asignado.
     */
    public static Medico nuevo(String nombres, String apellidos,
                                String correo, String contrasena,
                                String especialidad) {
        Medico m = new Medico();
        m.setNombres(nombres);
        m.setApellidos(apellidos);
        m.setCorreo(correo);
        m.setContrasena(contrasena);
        m.setRol(RolUsuario.MEDICO);
        m.setActivo(true);
        m.setEspecialidad(especialidad);
        return m;
    }
}
