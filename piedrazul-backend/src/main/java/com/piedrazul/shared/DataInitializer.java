package com.piedrazul.shared;

import com.piedrazul.configuracion.application.ConfiguracionService;
import com.piedrazul.configuracion.dto.ConfiguracionSistemaRequest;
import com.piedrazul.configuracion.dto.DisponibilidadMedicoRequest;
import com.piedrazul.medicos.application.MedicoService;
import com.piedrazul.medicos.dto.MedicoRequest;
import com.piedrazul.medicos.dto.MedicoResponse;
import com.piedrazul.sesion.domain.RolUsuario;
import com.piedrazul.sesion.domain.Usuario;
import com.piedrazul.sesion.infrastructure.persistence.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

/**
 * Carga datos iniciales necesarios para que la aplicacion funcione.
 * Usa existsByNumeroDocumento() antes de insertar — es idempotente:
 * si los datos ya existen (segunda vez que arranca el servidor) no los duplica.
 *
 * En produccion (ddl-auto=create la primera vez, luego validate),
 * esto corre una sola vez y crea los datos semilla.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MedicoService medicoService;
    private final ConfiguracionService configuracionService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("--- Cargando datos iniciales (idempotente) ---");

        crearAdmin();
        crearAgendador();

        crearUsuarioMedico("carlos.gomez",   "medico1234", "Carlos", "Gomez");
        crearUsuarioMedico("laura.martinez", "medico1234", "Laura",  "Martinez");

        MedicoResponse medico1 = crearMedico(
                "Carlos", "Gomez", "1234", "Medicina General",
                "Médico general con amplia experiencia en atención primaria y prevención de enfermedades crónicas.",
                "8 años de experiencia");
        MedicoResponse medico2 = crearMedico(
                "Laura", "Martinez", "5678", "Fisioterapia",
                "Fisioterapeuta especializada en rehabilitación musculoesquelética y terapia deportiva.",
                "10 años de experiencia");

        configurarDisponibilidad(
                medico1.getId(),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                LocalTime.of(8, 0), LocalTime.of(12, 0), 30);

        configurarDisponibilidad(
                medico2.getId(),
                Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
                LocalTime.of(14, 0), LocalTime.of(18, 0), 45);

        // Solo guarda configuracion si no existe ya
        if (!configuracionService.existeConfiguracion()) {
            ConfiguracionSistemaRequest cfg = new ConfiguracionSistemaRequest();
            cfg.setVentanaSemanas(4);
            configuracionService.guardarConfiguracionSistema(cfg);
        }

        log.info("--- Datos iniciales OK ---");
    }

    private void crearAdmin() {
        if (usuarioRepository.existsByNumeroDocumento("admin")) {
            log.info("Admin ya existe, omitiendo.");
            return;
        }
        Usuario admin = new Usuario();
        admin.setNombres("Administrador");
        admin.setApellidos("Piedrazul");
        admin.setNumeroDocumento("admin");
        admin.setContrasena(passwordEncoder.encode("admin1234"));
        admin.setRol(RolUsuario.ADMIN);
        admin.setActivo(true);
        usuarioRepository.save(admin);
        log.info("Admin creado.");
    }

    private void crearAgendador() {
        if (usuarioRepository.existsByNumeroDocumento("agendador")) {
            log.info("Agendador ya existe, omitiendo.");
            return;
        }
        Usuario agendador = new Usuario();
        agendador.setNombres("Recepcionista");
        agendador.setApellidos("Piedrazul");
        agendador.setNumeroDocumento("agendador");
        agendador.setContrasena(passwordEncoder.encode("agendador1234"));
        agendador.setRol(RolUsuario.AGENDADOR);
        agendador.setActivo(true);
        usuarioRepository.save(agendador);
        log.info("Agendador creado.");
    }

    private void crearUsuarioMedico(String numeroDocumento, String contrasena,
                                     String nombres, String apellidos) {
        if (usuarioRepository.existsByNumeroDocumento(numeroDocumento)) {
            log.info("Usuario médico '{}' ya existe, omitiendo.", numeroDocumento);
            return;
        }
        Usuario medico = new Usuario();
        medico.setNombres(nombres);
        medico.setApellidos(apellidos);
        medico.setNumeroDocumento(numeroDocumento);
        medico.setContrasena(passwordEncoder.encode(contrasena));
        medico.setRol(RolUsuario.MEDICO);
        medico.setActivo(true);
        usuarioRepository.save(medico);
        log.info("Usuario médico creado: {}", numeroDocumento);
    }

    private MedicoResponse crearMedico(String nombres, String apellidos,
                                        String numeroDocumento, String especialidad,
                                        String descripcion, String anosExperiencia) {
        // Si el medico ya existe en BD, busca y devuelve sin duplicar
        try {
            MedicoRequest req = new MedicoRequest();
            req.setNombres(nombres);
            req.setApellidos(apellidos);
            req.setNumeroDocumento(numeroDocumento);
            req.setEspecialidad(especialidad);
            req.setDescripcion(descripcion);
            req.setAnosExperiencia(anosExperiencia);
            return medicoService.crear(req);
        } catch (Exception e) {
            log.info("Medico '{}' ya existe, omitiendo creacion.", numeroDocumento);
            return medicoService.buscarPorDocumento(numeroDocumento);
        }
    }

    private void configurarDisponibilidad(Long medicoId, Set<DayOfWeek> dias,
                                           LocalTime inicio, LocalTime fin, int intervalo) {
        try {
            DisponibilidadMedicoRequest req = new DisponibilidadMedicoRequest();
            req.setMedicoId(medicoId);
            req.setDiasSemana(dias);
            req.setHoraInicio(inicio);
            req.setHoraFin(fin);
            req.setIntervaloMinutos(intervalo);
            configuracionService.guardarDisponibilidad(req);
        } catch (Exception e) {
            log.info("Disponibilidad para medico {} ya existe, omitiendo.", medicoId);
        }
    }
}