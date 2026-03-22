package com.piedrazul.pacientes.application;

import com.piedrazul.pacientes.domain.Paciente;
import com.piedrazul.pacientes.dto.PacienteRequest;
import com.piedrazul.pacientes.dto.PacienteResponse;
import com.piedrazul.pacientes.infrastructure.persistence.PacienteRepository;
import com.piedrazul.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    @Transactional
    public PacienteResponse crearOActualizar(PacienteRequest request) {
        Paciente paciente = pacienteRepository
                .findByNumeroDocumento(request.getNumeroDocumento())
                .orElseGet(Paciente::new);

        paciente.setNumeroDocumento(request.getNumeroDocumento());
        paciente.setNombres(request.getNombres());
        paciente.setApellidos(request.getApellidos());
        paciente.setCelular(request.getCelular());
        paciente.setGenero(request.getGenero());
        paciente.setFechaNacimiento(request.getFechaNacimiento());
        paciente.setCorreo(request.getCorreo());
        return toResponse(pacienteRepository.save(paciente));
    }

    @Transactional(readOnly = true)
    public Optional<PacienteResponse> buscarPorDocumento(String numeroDocumento) {
        return pacienteRepository.findByNumeroDocumento(numeroDocumento).map(this::toResponse);
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
                .id(p.getId()).numeroDocumento(p.getNumeroDocumento())
                .nombres(p.getNombres()).apellidos(p.getApellidos())
                .celular(p.getCelular()).genero(p.getGenero())
                .fechaNacimiento(p.getFechaNacimiento()).correo(p.getCorreo())
                .tieneUsuario(p.getUsuario() != null)
                .build();
    }
}
