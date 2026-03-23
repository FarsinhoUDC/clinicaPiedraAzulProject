package com.piedrazul.configuracion.infrastructure.persistence;

import com.piedrazul.configuracion.domain.DisponibilidadMedico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DisponibilidadMedicoRepository extends JpaRepository<DisponibilidadMedico, Long> {
    Optional<DisponibilidadMedico> findByMedicoId(Long medicoId);
}
