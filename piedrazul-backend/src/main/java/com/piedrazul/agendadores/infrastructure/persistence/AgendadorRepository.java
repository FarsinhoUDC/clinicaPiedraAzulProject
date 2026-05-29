package com.piedrazul.agendadores.infrastructure.persistence;

import com.piedrazul.agendadores.domain.Agendador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgendadorRepository extends JpaRepository<Agendador, Long> {

    Optional<Agendador> findByNumeroDocumento(String numeroDocumento);
}
