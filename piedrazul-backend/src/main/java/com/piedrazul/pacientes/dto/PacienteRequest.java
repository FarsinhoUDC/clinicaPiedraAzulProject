package com.piedrazul.pacientes.dto;

import com.piedrazul.pacientes.domain.Genero;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO para crear/identificar un paciente al agendar una cita.
 *
 * Con Keycloak, los usuarios solo tienen first_name y last_name;
 * los campos celular y genero son opcionales para no bloquear
 * el agendamiento. El backend los preserva si ya existen en BD.
 */
@Data
public class PacienteRequest {

    @NotBlank(message = "El numero de documento es requerido")
    private String numeroDocumento;

    @NotBlank(message = "Los nombres son requeridos")
    private String nombres;

    @NotBlank(message = "Los apellidos son requeridos")
    private String apellidos;

    private String correo;

    private String contrasena;

    // Opcional — no disponible en el JWT de Keycloak
    private String celular;

    // Opcional — no disponible en el JWT de Keycloak
    private Genero genero;

    private LocalDate fechaNacimiento;
}
