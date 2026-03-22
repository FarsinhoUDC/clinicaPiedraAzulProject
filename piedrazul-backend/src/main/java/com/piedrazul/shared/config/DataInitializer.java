package com.piedrazul.shared.config;

import com.piedrazul.configuracion.application.ConfiguracionService;
import com.piedrazul.configuracion.dto.ConfiguracionSistemaRequest;
import com.piedrazul.configuracion.dto.DisponibilidadMedicoRequest;
import com.piedrazul.medicos.application.MedicoService;
import com.piedrazul.medicos.dto.MedicoRequest;
import com.piedrazul.medicos.dto.MedicoResponse;
import com.piedrazul.usuarios.application.UsuarioService;
import com.piedrazul.usuarios.domain.Rol;
import com.piedrazul.usuarios.dto.RegistroUsuarioRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

/**
 * Carga datos iniciales para facilitar el desarrollo y pruebas.
 * Eliminar o comentar en produccion.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioService usuarioService;
    private final MedicoService medicoService;
    private final ConfiguracionService configuracionService;

    @Override
    public void run(String... args) {
        log.info("--- Cargando datos iniciales ---");

        crearUsuario("admin", "admin123", Rol.ADMIN);
        crearUsuario("agendador1", "agendador123", Rol.AGENDADOR);
        crearUsuario("paciente1", "paciente123", Rol.PACIENTE);

        MedicoResponse medico1 = crearMedico("Carlos", "Gomez", "Medicina General");
        MedicoResponse medico2 = crearMedico("Laura", "Martinez", "Fisioterapia");

        configurarDisponibilidad(medico1.getId(), 8, 0, 12, 0, 30,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
        configurarDisponibilidad(medico2.getId(), 14, 0, 18, 0, 45,
                Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY));

        ConfiguracionSistemaRequest cfg = new ConfiguracionSistemaRequest();
        cfg.setVentanaSemanas(4);
        configuracionService.guardarConfiguracionSistema(cfg);

        log.info("--- Datos iniciales cargados correctamente ---");
        log.info("Credenciales: admin/admin123 | agendador1/agendador123 | paciente1/paciente123");
    }

    private void crearUsuario(String username, String password, Rol rol) {
        try {
            RegistroUsuarioRequest req = new RegistroUsuarioRequest();
            req.setUsername(username); req.setPassword(password); req.setRol(rol);
            usuarioService.registrar(req);
        } catch (Exception e) { log.warn("Usuario {} ya existe", username); }
    }

    private MedicoResponse crearMedico(String nombres, String apellidos, String especialidad) {
        MedicoRequest req = new MedicoRequest();
        req.setNombres(nombres); req.setApellidos(apellidos); req.setEspecialidad(especialidad);
        return medicoService.crear(req);
    }

    private void configurarDisponibilidad(Long medicoId, int hIni, int mIni, int hFin, int mFin,
                                           int intervalo, Set<DayOfWeek> dias) {
        DisponibilidadMedicoRequest req = new DisponibilidadMedicoRequest();
        req.setMedicoId(medicoId);
        req.setDiasSemana(dias);
        req.setHoraInicio(LocalTime.of(hIni, mIni));
        req.setHoraFin(LocalTime.of(hFin, mFin));
        req.setIntervaloMinutos(intervalo);
        configuracionService.guardarDisponibilidad(req);
    }
}
