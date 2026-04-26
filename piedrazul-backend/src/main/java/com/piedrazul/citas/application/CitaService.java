package com.piedrazul.citas.application;

import com.piedrazul.citas.domain.Cita;
import com.piedrazul.citas.dto.CitaResponse;
import com.piedrazul.citas.infrastructure.persistence.CitaRepository;
import com.piedrazul.shared.exception.BusinessException;
import com.piedrazul.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository citaRepository;

    @Transactional
    public Cita guardar(Cita cita) {
        if (citaRepository.existsByMedicoIdAndFechaHora(
                cita.getMedico().getId(), cita.getFechaHora())) {
            throw new BusinessException(
                    "Ya existe una cita para el medico en esa fecha y hora");
        }
        return citaRepository.save(cita);
    }

    // HU-01
    @Transactional(readOnly = true)
    public List<CitaResponse> listarPorMedicoYFecha(Long medicoId, LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin    = fecha.plusDays(1).atStartOfDay();
        return citaRepository.findByMedicoIdAndFecha(medicoId, inicio, fin)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocalTime> obtenerHorasOcupadas(Long medicoId, LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin    = fecha.plusDays(1).atStartOfDay();
        return citaRepository.findFechaHorasByMedicoIdAndFecha(medicoId, inicio, fin)
                .stream().map(LocalDateTime::toLocalTime).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CitaResponse obtenerPorId(Long id) {
        return citaRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", id));
    }

    @Transactional(readOnly = true)
    public List<CitaResponse> listarPorPaciente(Long pacienteId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime inicio = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fin = now.plusMonths(12).withHour(0).withMinute(0).withSecond(0);
        return citaRepository.findByPacienteIdAndFecha(pacienteId, inicio, fin)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Busca las citas de un paciente por su número de documento (preferred_username de Keycloak).
     * Incluye citas pasadas y futuras en una ventana de 12 meses hacia atrás y adelante.
     */
    @Transactional(readOnly = true)
    public List<CitaResponse> listarPorDocumentoPaciente(String numeroDocumento) {
        return citaRepository.findByPacienteNumeroDocumento(numeroDocumento)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public CitaResponse toResponse(Cita c) {
        return CitaResponse.builder()
                .id(c.getId())
                .pacienteId(c.getPaciente().getId())
                .nombrePaciente(c.getPaciente().getNombres() + " " + c.getPaciente().getApellidos())
                .documentoPaciente(c.getPaciente().getNumeroDocumento())
                .celularPaciente(c.getPaciente().getCelular())
                .medicoId(c.getMedico().getId())
                .nombreMedico(c.getMedico().getNombres() + " " + c.getMedico().getApellidos())
                .fechaHora(c.getFechaHora())
                .origen(c.getOrigen())
                .creadoEn(c.getCreadoEn())
                .build();
    }
}
