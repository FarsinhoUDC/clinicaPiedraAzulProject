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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * PATRON FACADE: coordina PacienteService, MedicoService,
 * FranjaHorariaService y CitaService para crear una cita completa.
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

        // 1. Upsert del paciente por numero de documento
        pacienteService.crearOActualizar(request.getPaciente());
        Paciente paciente = pacienteService.obtenerEntidadPorDocumento(
                request.getPaciente().getNumeroDocumento());

        // 2. Verificar que el medico existe
        Medico medico = medicoService.obtenerPorId(request.getMedicoId());

        // 3. Validar franja horaria
        LocalDate fecha = request.getFechaHora().toLocalDate();
        LocalTime hora  = request.getFechaHora().toLocalTime();
        DisponibilidadMedico disp = franjaHorariaService.obtenerDisponibilidad(medico.getId());
        validarFranja(disp, fecha, hora);

        // 4. Persistir cita
        Cita cita = Cita.builder()
                .paciente(paciente)
                .medico(medico)
                .fechaHora(request.getFechaHora())
                .origen(origen)
                .build();

        return citaService.toResponse(citaService.guardar(cita));
    }

    @Transactional
    public CitaResponse reagendarCita(Long citaId, com.piedrazul.citas.dto.ReagendarCitaRequest request, String usuarioResponsable) {
        // 1. Obtener la cita
        Cita cita = citaService.obtenerEntidadPorId(citaId);

        // 2. Validar estado
        if (cita.getEstado() == com.piedrazul.citas.domain.EstadoCita.CANCELADA || 
            cita.getEstado() == com.piedrazul.citas.domain.EstadoCita.FINALIZADA) {
            throw new BusinessException("No se puede reagendar una cita que está " + cita.getEstado().name());
        }

        // 3. Validar disponibilidad
        LocalDate nuevaFecha = request.getNuevaFechaHora().toLocalDate();
        LocalTime nuevaHora = request.getNuevaFechaHora().toLocalTime();
        DisponibilidadMedico disp = franjaHorariaService.obtenerDisponibilidad(cita.getMedico().getId());
        validarFranja(disp, nuevaFecha, nuevaHora);

        // 4. Crear HistorialCita
        com.piedrazul.citas.domain.HistorialCita historial = com.piedrazul.citas.domain.HistorialCita.builder()
                .cita(cita)
                .fechaAnterior(cita.getFechaHora())
                .fechaNueva(request.getNuevaFechaHora())
                .motivo(request.getMotivo())
                .usuarioResponsable(usuarioResponsable)
                .build();

        // 5. Actualizar Cita
        cita.setFechaHora(request.getNuevaFechaHora());
        cita.setEstado(com.piedrazul.citas.domain.EstadoCita.REAGENDADA);
        cita.getHistoriales().add(historial);

        // 6. Guardar y retornar
        return citaService.toResponse(citaService.guardar(cita));
    }

    private void validarFranja(DisponibilidadMedico disp, LocalDate fecha, LocalTime hora) {
        if (!disp.getDiasSemana().contains(fecha.getDayOfWeek())) {
            throw new BusinessException(
                    "El medico no atiende el " + fecha.getDayOfWeek());
        }
        if (hora.isBefore(disp.getHoraInicio()) || hora.isAfter(disp.getHoraFin())) {
            throw new BusinessException(
                    "Hora fuera de la franja del medico (" +
                    disp.getHoraInicio() + " - " + disp.getHoraFin() + ")");
        }
        long mins = Duration.between(disp.getHoraInicio(), hora).toMinutes();
        if (mins < 0 || mins % disp.getIntervaloMinutos() != 0) {
            throw new BusinessException(
                    "La hora no corresponde a un slot valido. " +
                    "Intervalo: " + disp.getIntervaloMinutos() + " min");
        }
    }
}
