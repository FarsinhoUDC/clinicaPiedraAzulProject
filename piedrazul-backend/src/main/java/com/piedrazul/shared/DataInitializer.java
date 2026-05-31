package com.piedrazul.shared;

import com.piedrazul.configuracion.application.ConfiguracionService;
import com.piedrazul.configuracion.dto.ConfiguracionSistemaRequest;
import com.piedrazul.configuracion.dto.DisponibilidadMedicoRequest;
import com.piedrazul.medicos.application.MedicoService;
import com.piedrazul.medicos.dto.MedicoRequest;
import com.piedrazul.medicos.dto.MedicoResponse;
import com.piedrazul.medicos.infrastructure.persistence.MedicoRepository;
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
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MedicoService medicoService;
    private final MedicoRepository medicoRepository;
    private final ConfiguracionService configuracionService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("--- Cargando datos iniciales (idempotente) ---");

        crearAdmin();
        crearAgendador();

        MedicoResponse medico1 = crearMedico(
                "Carlos", "Gomez", "1234", "Medicina General",
                "Médico general con amplia experiencia en atención primaria y prevención de enfermedades crónicas.",
                "8 años de experiencia");

        MedicoResponse medico2 = crearMedico(
                "Laura", "Martinez", "5678", "Fisioterapia",
                "Fisioterapeuta especializada en rehabilitación musculoesquelética y terapia deportiva.",
                "10 años de experiencia");

        if (medico1 != null) {
            configurarDisponibilidad(
                    medico1.getId(),
                    Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                    LocalTime.of(8, 0), LocalTime.of(12, 0), 30);
        }

        if (medico2 != null) {
            configurarDisponibilidad(
                    medico2.getId(),
                    Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
                    LocalTime.of(14, 0), LocalTime.of(18, 0), 45);
        }

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

    /**
     * Retorna el MedicoResponse si se crea o ya existe, null si hay error irrecuperable.
     * Nunca lanza excepcion para no bloquear el arranque.
     */
    private MedicoResponse crearMedico(String nombres, String apellidos,
                                        String numeroDocumento, String especialidad,
                                        String descripcion, String anosExperiencia) {
        // Si ya existe en la tabla medicos, devuelve directamente
        Optional<MedicoResponse> existente = medicoRepository
                .findByNumeroDocumento(numeroDocumento)
                .map(medicoService::toResponse);

        if (existente.isPresent()) {
            log.info("Medico '{}' ya existe, omitiendo creacion.", numeroDocumento);
            return existente.get();
        }

        // Si existe en usuarios pero no en medicos, hay inconsistencia — omitir
        if (usuarioRepository.existsByNumeroDocumento(numeroDocumento)) {
            log.warn("Usuario '{}' existe en usuarios pero no en medicos. Omitiendo.", numeroDocumento);
            return null;
        }

        try {
            MedicoRequest req = new MedicoRequest();
            req.setNombres(nombres);
            req.setApellidos(apellidos);
            req.setNumeroDocumento(numeroDocumento);
            req.setEspecialidad(especialidad);
            req.setDescripcion(descripcion);
            req.setAnosExperiencia(anosExperiencia);
            MedicoResponse response = medicoService.crear(req);
            log.info("Medico '{}' creado.", numeroDocumento);
            return response;
        } catch (Exception e) {
            log.warn("No se pudo crear medico '{}': {}", numeroDocumento, e.getMessage());
            return null;
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
            log.warn("Disponibilidad para medico {} ya existe o fallo: {}", medicoId, e.getMessage());
        }
    }
}