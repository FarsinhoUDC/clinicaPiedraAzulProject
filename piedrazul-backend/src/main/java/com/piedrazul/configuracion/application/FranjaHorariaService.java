package com.piedrazul.configuracion.application;

import com.piedrazul.configuracion.domain.DisponibilidadMedico;
import com.piedrazul.configuracion.dto.FranjaHorariaResponse;
import com.piedrazul.configuracion.infrastructure.persistence.DisponibilidadMedicoRepository;
import com.piedrazul.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio que genera las franjas horarias disponibles para un medico en una fecha dada.
 * Implementa el patron Strategy: la logica de generacion es intercambiable
 * y puede variar segun las reglas de negocio de cada medico.
 */
@Service
@RequiredArgsConstructor
public class FranjaHorariaService {

    private final DisponibilidadMedicoRepository disponibilidadMedicoRepository;

    /**
     * Genera todas las franjas del medico para la fecha indicada,
     * marcando si estan disponibles o ya ocupadas.
     *
     * @param medicoId   ID del medico
     * @param fecha      Fecha para la que se generan las franjas
     * @param horasOcupadas Lista de horas ya tomadas por citas existentes
     */
    public List<FranjaHorariaResponse> generarFranjas(Long medicoId, LocalDate fecha,
                                                       List<LocalTime> horasOcupadas) {
        DisponibilidadMedico disponibilidad = disponibilidadMedicoRepository
                .findByMedicoId(medicoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay disponibilidad configurada para el medico " + medicoId));

        // Verificar que el medico atiende ese dia de la semana
        if (!disponibilidad.getDiasSemana().contains(fecha.getDayOfWeek())) {
            return List.of();
        }

        List<FranjaHorariaResponse> franjas = new ArrayList<>();
        LocalTime cursor = disponibilidad.getHoraInicio();

        while (!cursor.isAfter(disponibilidad.getHoraFin().minusMinutes(disponibilidad.getIntervaloMinutos()))) {
            boolean ocupada = horasOcupadas.contains(cursor);
            franjas.add(new FranjaHorariaResponse(cursor, !ocupada));
            cursor = cursor.plusMinutes(disponibilidad.getIntervaloMinutos());
        }

        return franjas;
    }

    public DisponibilidadMedico obtenerDisponibilidad(Long medicoId) {
        return disponibilidadMedicoRepository.findByMedicoId(medicoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay disponibilidad configurada para el medico " + medicoId));
    }
}
