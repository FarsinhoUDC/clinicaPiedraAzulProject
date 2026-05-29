package com.piedrazul.medicos.domain;

import com.piedrazul.sesion.domain.RolUsuario;
import com.piedrazul.sesion.domain.Usuario;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


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

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String anosExperiencia;

    private String fotoUrl;

    /**
     * Constructor completo con todos los campos del médico.
     */
    public static Medico nuevo(String nombres, String apellidos,
                                String correo, String contrasena,
                                String numeroDocumento,
                                String especialidad,
                                String celular,
                                com.piedrazul.pacientes.domain.Genero genero,
                                java.time.LocalDate fechaNacimiento,
                                String descripcion,
                                String anosExperiencia,
                                String fotoUrl) {
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
        m.setDescripcion(descripcion);
        m.setAnosExperiencia(anosExperiencia);
        m.setFotoUrl(fotoUrl);
        return m;
    }

    /**
     * Constructor de compatibilidad (9 params) para tests existentes.
     * Los campos descripcion, anosExperiencia y fotoUrl quedan en null.
     */
    public static Medico nuevo(String nombres, String apellidos,
                                String correo, String contrasena,
                                String numeroDocumento,
                                String especialidad,
                                String celular,
                                com.piedrazul.pacientes.domain.Genero genero,
                                java.time.LocalDate fechaNacimiento) {
        return nuevo(nombres, apellidos, correo, contrasena,
                numeroDocumento, especialidad, celular, genero,
                fechaNacimiento, null, null, null);
    }
}
