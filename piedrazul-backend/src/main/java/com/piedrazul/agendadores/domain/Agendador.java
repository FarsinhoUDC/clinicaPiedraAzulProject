package com.piedrazul.agendadores.domain;

import com.piedrazul.pacientes.domain.Genero;
import com.piedrazul.sesion.domain.RolUsuario;
import com.piedrazul.sesion.domain.Usuario;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "agendadores")
@PrimaryKeyJoinColumn(name = "usuario_id")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Agendador extends Usuario {

    @Column(nullable = true)
    private String celular;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Genero genero;

    private LocalDate fechaNacimiento;

    public static Agendador nuevo(String nombres, String apellidos,
                                  String correo, String contrasena,
                                  String numeroDocumento, String celular,
                                  Genero genero, LocalDate fechaNacimiento) {
        Agendador a = new Agendador();
        a.setNombres(nombres);
        a.setApellidos(apellidos);
        a.setCorreo(correo);
        a.setContrasena(contrasena);
        a.setRol(RolUsuario.AGENDADOR);
        a.setActivo(true);
        a.setNumeroDocumento(numeroDocumento);
        a.setCelular(celular != null ? celular : "");
        a.setGenero(genero);
        a.setFechaNacimiento(fechaNacimiento);
        return a;
    }
}
