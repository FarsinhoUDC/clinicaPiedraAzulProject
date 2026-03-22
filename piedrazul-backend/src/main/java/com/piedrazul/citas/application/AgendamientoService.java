package com.piedrazul.citas.application;

import com.piedrazul.citas.domain.Cita;
import com.piedrazul.citas.domain.OrigenCita;
import com.piedrazul.citas.dto.CitaResponse;
import com.piedrazul.citas.dto.CrearCitaRequest;
import com.piedrazul.configuracion.application.FranjaHorariaService;
import com.piedrazul.configuracion.domain.DisponibilidadMedico;
import com.piedrazul.medicos.application.MedicoService;
import com.piedrazul.medicos.domain.Medico;
import com.piedrazul.pacientes.application.PacienteService;
import com.piedrazul.pacientes.domain.Paciente;
import com.piedrazul.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * PATRÓN FACADE:
 * Coordina PacienteService, MedicoService, FranjaHorariaService y CitaService
 * para ejecutar el caso de uso completo de creación de cita.
 * El controlador solo interactúa con esta fachada.
 */
@Service
@RequiredArgsConstructor
public class AgendamientoService {

    private final PacienteService pacienteService;
    private final MedicoService medicoService;
    private final FranjaHorariaService franjaHorariaService;
    private final CitaService citaService;

    @Transactional
    public CitaResponse crearCita(CrearCitaRequest request, OrigenCita origen) {

        // 1. Crear o actualizar paciente (upsert por documento)
        pacienteService.crearOActualizar(request.getPaciente());
        Paciente paciente = pacienteService.obtenerEntidadPorDocumento(
                request.getPaciente().getNumeroDocumento());

        // 2. Verificar que el médico existe y está activo
        Medico medico = medicoService.obtenerPorId(request.getMedicoId());

        // 3. Validar que la hora cae dentro de una franja válida del médico
        LocalDate fecha = request.getFechaHora().toLocalDate();
        LocalTime hora  = request.getFechaHora().toLocalTime();
        DisponibilidadMedico disponibilidad =
                franjaHorariaService.obtenerDisponibilidad(medico.getId());

        validarFranjaHoraria(disponibilidad, fecha, hora);

        // 4. Construir y persistir la cita
        Cita cita = Cita.builder()
                .paciente(paciente)
                .medico(medico)
                .fechaHora(request.getFechaHora())
                .origen(origen)
                .build();

        return citaService.toResponse(citaService.guardar(cita));
    }

    // ──────────────────────────────────────────────────────────────
    // Validaciones de negocio
    // ──────────────────────────────────────────────────────────────

    private void validarFranjaHoraria(DisponibilidadMedico disp,
                                       LocalDate fecha, LocalTime hora) {
        // Validar día de la semana
        if (!disp.getDiasSemana().contains(fecha.getDayOfWeek())) {
            throw new BusinessException(
                    "El médico no atiende el " + fecha.getDayOfWeek()
                    + ". Días disponibles: " + disp.getDiasSemana());
        }

        // Validar rango horario
        if (hora.isBefore(disp.getHoraInicio()) || hora.isAfter(disp.getHoraFin())) {
            throw new BusinessException(
                    "La hora " + hora + " está fuera de la franja del médico ("
                    + disp.getHoraInicio() + " - " + disp.getHoraFin() + ")");
        }

        // Validar que la hora corresponde a un slot del intervalo
        long minutosDesdeInicio = java.time.Duration
                .between(disp.getHoraInicio(), hora).toMinutes();
        if (minutosDesdeInicio < 0 || minutosDesdeInicio % disp.getIntervaloMinutos() != 0) {
            throw new BusinessException(
                    "La hora no corresponde a ningún slot válido del médico. "
                    + "Intervalo: " + disp.getIntervaloMinutos() + " minutos");
        }
    }
}
