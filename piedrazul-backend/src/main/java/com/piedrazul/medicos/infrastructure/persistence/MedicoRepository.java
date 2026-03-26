package com.piedrazul.medicos.infrastructure.persistence;

import com.piedrazul.medicos.domain.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicoRepository extends JpaRepository<Medico, Long> {

    List<Medico> findByActivoTrue();

    boolean existsByCorreo(String correo);
}
