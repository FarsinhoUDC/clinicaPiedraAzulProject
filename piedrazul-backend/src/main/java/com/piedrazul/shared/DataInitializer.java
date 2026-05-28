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
        log.info("--- Cargando datos iniciales ---");

        crearAdmin();
        crearAgendador();

        // Usuarios de sesión para los médicos (rol MEDICO en Keycloak / JWT)
        crearUsuarioMedico("carlos.gomez",  "medico1234", "Carlos",  "Gomez");
        crearUsuarioMedico("laura.martinez", "medico1234", "Laura",   "Martinez");

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

        ConfiguracionSistemaRequest cfg = new ConfiguracionSistemaRequest();
        cfg.setVentanaSemanas(4);
        configuracionService.guardarConfiguracionSistema(cfg);

        log.info("--- Datos iniciales cargados. Medico1 id={}, Medico2 id={} ---",
                medico1.getId(), medico2.getId());
        log.info("--- Admin:     admin / admin1234 ---");
        log.info("--- Agendador: agendador / agendador1234 ---");
        log.info("--- Medico 1:  carlos.gomez / medico1234 ---");
        log.info("--- Medico 2:  laura.martinez / medico1234 ---");
    }

    private void crearAdmin() {
        String numeroDocumento = "admin";
        if (usuarioRepository.existsByNumeroDocumento(numeroDocumento)) {
            log.info("Admin ya existe, omitiendo creacion.");
            return;
        }
        Usuario admin = new Usuario();
        admin.setNombres("Administrador");
        admin.setApellidos("Piedrazul");
        admin.setNumeroDocumento(numeroDocumento);
        admin.setContrasena(passwordEncoder.encode("admin1234"));
        admin.setRol(RolUsuario.ADMIN);
        admin.setActivo(true);
        usuarioRepository.save(admin);
        log.info("Admin creado: {}", numeroDocumento);
    }

    private void crearAgendador() {
        String numeroDocumento = "agendador";
        if (usuarioRepository.existsByNumeroDocumento(numeroDocumento)) {
            log.info("Agendador ya existe, omitiendo creacion.");
            return;
        }
        Usuario agendador = new Usuario();
        agendador.setNombres("Recepcionista");
        agendador.setApellidos("Piedrazul");
        agendador.setNumeroDocumento(numeroDocumento);
        agendador.setContrasena(passwordEncoder.encode("agendador1234"));
        agendador.setRol(RolUsuario.AGENDADOR);
        agendador.setActivo(true);
        usuarioRepository.save(agendador);
        log.info("Agendador creado: {}", numeroDocumento);
    }

    /**
     * Crea un usuario de sesión con rol MEDICO.
     * El numeroDocumento actúa como preferred_username en Keycloak,
     * por lo que debe coincidir con el username configurado allí.
     */
    private void crearUsuarioMedico(String numeroDocumento, String contrasena,
                                     String nombres, String apellidos) {
        if (usuarioRepository.existsByNumeroDocumento(numeroDocumento)) {
            log.info("Usuario médico '{}' ya existe, omitiendo creacion.", numeroDocumento);
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
                                        String numeroDocumento,
                                        String especialidad,
                                        String descripcion,
                                        String anosExperiencia) {
        MedicoRequest req = new MedicoRequest();
        req.setNombres(nombres);
        req.setApellidos(apellidos);
        req.setNumeroDocumento(numeroDocumento);
        req.setEspecialidad(especialidad);
        req.setDescripcion(descripcion);
        req.setAnosExperiencia(anosExperiencia);
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