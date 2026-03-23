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
 * PATRON STRATEGY: genera las franjas horarias disponibles para un medico
 * en una fecha dada. La logica puede variar segun las reglas de disponibilidad.
 */
@Service
@RequiredArgsConstructor
public class FranjaHorariaService {

    private final DisponibilidadMedicoRepository disponibilidadMedicoRepository;

    public List<FranjaHorariaResponse> generarFranjas(Long medicoId, LocalDate fecha,
                                                       List<LocalTime> horasOcupadas) {
        DisponibilidadMedico disp = obtenerDisponibilidad(medicoId);

        if (!disp.getDiasSemana().contains(fecha.getDayOfWeek())) {
            return List.of();
        }

        List<FranjaHorariaResponse> franjas = new ArrayList<>();
        LocalTime cursor = disp.getHoraInicio();
        LocalTime limite = disp.getHoraFin().minusMinutes(disp.getIntervaloMinutos());

        while (!cursor.isAfter(limite)) {
            franjas.add(new FranjaHorariaResponse(cursor, !horasOcupadas.contains(cursor)));
            cursor = cursor.plusMinutes(disp.getIntervaloMinutos());
        }

        return franjas;
    }

    public DisponibilidadMedico obtenerDisponibilidad(Long medicoId) {
        return disponibilidadMedicoRepository.findByMedicoId(medicoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay disponibilidad configurada para el medico " + medicoId));
    }
}
