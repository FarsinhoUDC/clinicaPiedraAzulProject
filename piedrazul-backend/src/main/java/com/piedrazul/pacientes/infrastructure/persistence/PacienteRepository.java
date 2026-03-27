package com.piedrazul.pacientes.infrastructure.persistence;

import com.piedrazul.pacientes.domain.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByNumeroDocumento(String numeroDocumento);
    Optional<Paciente> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
