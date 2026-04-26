package com.piedrazul.citas.infrastructure.persistence;

import com.piedrazul.citas.domain.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    // HU-01: listar citas de un medico en un rango de dia completo
    @Query("SELECT c FROM Cita c " +
           "WHERE c.medico.id = :medicoId " +
           "AND c.fechaHora >= :inicio " +
           "AND c.fechaHora < :fin " +
           "ORDER BY c.fechaHora ASC")
    List<Cita> findByMedicoIdAndFecha(
            @Param("medicoId") Long medicoId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    // Para calcular franjas ocupadas
    @Query("SELECT c.fechaHora FROM Cita c " +
           "WHERE c.medico.id = :medicoId " +
           "AND c.fechaHora >= :inicio " +
           "AND c.fechaHora < :fin")
    List<LocalDateTime> findFechaHorasByMedicoIdAndFecha(
            @Param("medicoId") Long medicoId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    boolean existsByMedicoIdAndFechaHora(Long medicoId, LocalDateTime fechaHora);

    @Query("SELECT c FROM Cita c " +
           "WHERE c.paciente.id = :pacienteId " +
           "AND c.fechaHora >= :inicio " +
           "AND c.fechaHora < :fin " +
           "ORDER BY c.fechaHora ASC")
    List<Cita> findByPacienteIdAndFecha(
            @Param("pacienteId") Long pacienteId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    /**
     * Busca citas por número de documento del paciente.
     * Usado en el endpoint /api/citas/mis-citas con JWT de Keycloak.
     */
    @Query("SELECT c FROM Cita c " +
           "WHERE c.paciente.numeroDocumento = :numeroDocumento " +
           "ORDER BY c.fechaHora ASC")
    List<Cita> findByPacienteNumeroDocumento(
            @Param("numeroDocumento") String numeroDocumento);
}
