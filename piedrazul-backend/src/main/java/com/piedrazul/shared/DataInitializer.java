package com.piedrazul.shared;

import com.piedrazul.configuracion.application.ConfiguracionService;
import com.piedrazul.configuracion.dto.ConfiguracionSistemaRequest;
import com.piedrazul.configuracion.dto.DisponibilidadMedicoRequest;
import com.piedrazul.medicos.application.MedicoService;
import com.piedrazul.medicos.dto.MedicoRequest;
import com.piedrazul.medicos.dto.MedicoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

/**
 * Carga datos iniciales para desarrollo y pruebas.
 * Eliminar o comentar en produccion.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MedicoService medicoService;
    private final ConfiguracionService configuracionService;

    @Override
    public void run(String... args) {
        log.info("--- Cargando datos iniciales ---");

        MedicoResponse medico1 = crearMedico("Carlos", "Gomez", "Medicina General");
        MedicoResponse medico2 = crearMedico("Laura", "Martinez", "Fisioterapia");

        configurarDisponibilidad(
                medico1.getId(),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                LocalTime.of(8, 0), LocalTime.of(12, 0), 30);

        configurarDisponibilidad(
                medico2.getId(),
                Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
                LocalTime.of(14, 0), LocalTime.of(18, 0), 45);

        ConfiguracionSistemaRequest cfg = new ConfiguracionSistemaRequest();
        cfg.setVentanaSemanas(4);
        configuracionService.guardarConfiguracionSistema(cfg);

        log.info("--- Datos iniciales cargados. Medico1 id={}, Medico2 id={} ---",
                medico1.getId(), medico2.getId());
    }

    private MedicoResponse crearMedico(String nombres, String apellidos, String especialidad) {
        MedicoRequest req = new MedicoRequest();
        req.setNombres(nombres);
        req.setApellidos(apellidos);
        req.setEspecialidad(especialidad);
        return medicoService.crear(req);
    }

    private void configurarDisponibilidad(Long medicoId, Set<DayOfWeek> dias,
                                           LocalTime inicio, LocalTime fin, int intervalo) {
        DisponibilidadMedicoRequest req = new DisponibilidadMedicoRequest();
        req.setMedicoId(medicoId);
        req.setDiasSemana(dias);
        req.setHoraInicio(inicio);
        req.setHoraFin(fin);
        req.setIntervaloMinutos(intervalo);
        configuracionService.guardarDisponibilidad(req);
    }
}
