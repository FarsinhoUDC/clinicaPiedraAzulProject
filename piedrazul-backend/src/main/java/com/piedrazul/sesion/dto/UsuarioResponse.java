package com.piedrazul.sesion.dto;

import com.piedrazul.sesion.domain.RolUsuario;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsuarioResponse {

    private Long id;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String correo;
    private RolUsuario rol;
    private boolean activo;
    private String celular;
    private String genero;
}
