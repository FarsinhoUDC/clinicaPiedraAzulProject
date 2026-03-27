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

        MedicoResponse medico1 = crearMedico(
                "Carlos", "Gomez", "carlos.gomez@piedrazul.com", "1234", "Medicina General");
        MedicoResponse medico2 = crearMedico(
                "Laura", "Martinez", "laura.martinez@piedrazul.com", "1234", "Fisioterapia");

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
        log.info("--- Admin: admin@piedrazul.com / admin1234 ---");
    }

    private void crearAdmin() {
        String correo = "admin@piedrazul.com";
        if (usuarioRepository.existsByCorreo(correo)) {
            log.info("Admin ya existe, omitiendo creacion.");
            return;
        }
        Usuario admin = new Usuario();
        admin.setNombres("Administrador");
        admin.setApellidos("Piedrazul");
        admin.setCorreo(correo);
        admin.setContrasena(passwordEncoder.encode("admin1234"));
        admin.setRol(RolUsuario.ADMIN);
        admin.setActivo(true);
        usuarioRepository.save(admin);
        log.info("Admin creado: {}", correo);
    }

    private MedicoResponse crearMedico(String nombres, String apellidos,
                                        String correo, String contrasena,
                                        String especialidad) {
        MedicoRequest req = new MedicoRequest();
        req.setNombres(nombres);
        req.setApellidos(apellidos);
        req.setCorreo(correo);
        req.setContrasena(contrasena);
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