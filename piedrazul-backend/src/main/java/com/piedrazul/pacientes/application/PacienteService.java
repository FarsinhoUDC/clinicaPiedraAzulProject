package com.piedrazul.pacientes.application;

import com.piedrazul.pacientes.domain.Paciente;
import com.piedrazul.pacientes.dto.PacienteRequest;
import com.piedrazul.pacientes.dto.PacienteResponse;
import com.piedrazul.pacientes.infrastructure.KeycloakService;
import com.piedrazul.pacientes.infrastructure.persistence.PacienteRepository;
import com.piedrazul.shared.exception.BusinessException;
import com.piedrazul.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final KeycloakService keycloakService;

    @Transactional
    public PacienteResponse crear(PacienteRequest request) {
        if (pacienteRepository.findByNumeroDocumento(request.getNumeroDocumento()).isPresent()) {
            throw new BusinessException("Ya existe un paciente registrado con el documento "
                    + request.getNumeroDocumento());
        }
        return crearNuevo(request);
    }

    @Transactional
    public PacienteResponse crearOActualizar(PacienteRequest request) {

        Optional<Paciente> existente = pacienteRepository
                .findByNumeroDocumento(request.getNumeroDocumento());

        if (existente.isPresent()) {
            Paciente p = existente.get();
            p.setNombres(request.getNombres());
            p.setApellidos(request.getApellidos());
            p.setCorreo(request.getCorreo());
            p.setCelular(request.getCelular());
            p.setGenero(request.getGenero());
            p.setFechaNacimiento(request.getFechaNacimiento());
            return toResponse(pacienteRepository.save(p));
        }

        return crearNuevo(request);
    }

    private PacienteResponse crearNuevo(PacienteRequest request) {
        String encodedPassword = request.getContrasena() != null
                ? passwordEncoder.encode(request.getContrasena())
                : passwordEncoder.encode("DEFAULT_PASS");

        Paciente paciente = Paciente.nuevo(
                    request.getNombres(),
                    request.getApellidos(),
                    request.getCorreo(),
                    encodedPassword,
                    request.getNumeroDocumento(),
                    request.getCelular(),
                    request.getGenero(),
                    request.getFechaNacimiento()
            );

        Paciente saved = pacienteRepository.save(paciente);
        
        keycloakService.crearUsuario(request);
        
        return toResponse(saved);
    }

    @Transactional
    public Optional<PacienteResponse> buscarPorDocumento(String numeroDocumento) {
        // 1. Buscar en BD local
        var local = pacienteRepository.findByNumeroDocumento(numeroDocumento);
        if (local.isPresent()) return local.map(this::toResponse);

        // 2. No existe localmente → buscar en Keycloak
        var keycloakUser = keycloakService.buscarUsuario(numeroDocumento);
        if (keycloakUser.isEmpty()) return Optional.empty();

        // 3. Existe en Keycloak → crear en BD local
        var kc = keycloakUser.get();
        var nuevo = Paciente.nuevo(
                kc.getFirstName() != null ? kc.getFirstName() : "",
                kc.getLastName() != null ? kc.getLastName() : "",
                kc.getEmail() != null ? kc.getEmail() : numeroDocumento + "@sin-correo.com",
                "KEYCLOAK_MANAGED",
                numeroDocumento,
                null, null, null
        );
        var saved = pacienteRepository.save(nuevo);
        log.info("Paciente {} creado desde Keycloak bajo demanda", numeroDocumento);
        return Optional.of(toResponse(saved));
    }

    @Transactional(readOnly = true)
    public Paciente obtenerEntidadPorDocumento(String doc) {
        return pacienteRepository.findByNumeroDocumento(doc)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", doc));
    }

    @Transactional(readOnly = true)
    public Paciente obtenerPorId(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", id));
    }

    public PacienteResponse toResponse(Paciente p) {
        return PacienteResponse.builder()
                .id(p.getId())
                .numeroDocumento(p.getNumeroDocumento())
                .nombres(p.getNombres())
                .apellidos(p.getApellidos())
                .correo(p.getCorreo())
                .celular(p.getCelular())
                .genero(p.getGenero())
                .fechaNacimiento(p.getFechaNacimiento())
                .build();
    }
}
