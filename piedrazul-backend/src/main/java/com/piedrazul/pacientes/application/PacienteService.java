package com.piedrazul.pacientes.application;

import com.piedrazul.pacientes.domain.Paciente;
import com.piedrazul.pacientes.dto.PacienteRequest;
import com.piedrazul.pacientes.dto.PacienteResponse;
import com.piedrazul.pacientes.infrastructure.persistence.PacienteRepository;
import com.piedrazul.shared.exception.BusinessException;
import com.piedrazul.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public PacienteResponse crearOActualizar(PacienteRequest request) {

        Optional<Paciente> existente = pacienteRepository
                .findByNumeroDocumento(request.getNumeroDocumento());

        if (existente.isPresent()) {
            return toResponse(existente.get());
        }

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

        return toResponse(pacienteRepository.save(paciente));
    }

    @Transactional(readOnly = true)
    public Optional<PacienteResponse> buscarPorDocumento(String numeroDocumento) {
        return pacienteRepository.findByNumeroDocumento(numeroDocumento)
                .map(this::toResponse);
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
