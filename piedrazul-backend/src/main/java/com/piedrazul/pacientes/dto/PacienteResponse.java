package com.piedrazul.pacientes.dto;

import com.piedrazul.pacientes.domain.Genero;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data @Builder
public class PacienteResponse {
    private Long id;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String celular;
    private Genero genero;
    private LocalDate fechaNacimiento;
    private String correo;
    private boolean tieneUsuario;
}
