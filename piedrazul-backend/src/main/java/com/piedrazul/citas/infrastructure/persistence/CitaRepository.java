package com.piedrazul.citas.infrastructure.persistence;

import com.piedrazul.citas.domain.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Citas de un médico en una fecha (HU-01).
     * Compara el rango del día completo para compatibilidad con H2 y PostgreSQL.
     */
    @Query("SELECT c FROM Cita c " +
           "WHERE c.medico.id = :medicoId " +
           "AND c.fechaHora >= :inicio " +
           "AND c.fechaHora < :fin " +
           "ORDER BY c.fechaHora ASC")
    List<Cita> findByMedicoIdAndFecha(
            @Param("medicoId") Long medicoId,
            @Param("inicio")   LocalDateTime inicio,
            @Param("fin")      LocalDateTime fin);

    /**
     * Horas ya ocupadas para un médico en una fecha.
     * Usadas por FranjaHorariaService para marcar slots como no disponibles.
     */
    @Query("SELECT c.fechaHora FROM Cita c " +
           "WHERE c.medico.id = :medicoId " +
           "AND c.fechaHora >= :inicio " +
           "AND c.fechaHora < :fin")
    List<LocalDateTime> findFechaHorasByMedicoIdAndFecha(
            @Param("medicoId") Long medicoId,
            @Param("inicio")   LocalDateTime inicio,
            @Param("fin")      LocalDateTime fin);

    boolean existsByMedicoIdAndFechaHora(Long medicoId, LocalDateTime fechaHora);
}
