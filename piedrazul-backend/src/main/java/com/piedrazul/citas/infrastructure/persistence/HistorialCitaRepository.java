package com.piedrazul.citas.infrastructure.persistence;

import com.piedrazul.citas.domain.HistorialCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialCitaRepository extends JpaRepository<HistorialCita, Long> {
}
