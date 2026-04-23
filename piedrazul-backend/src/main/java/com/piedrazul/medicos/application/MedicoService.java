package com.piedrazul.medicos.application;

import com.piedrazul.medicos.domain.Medico;
import com.piedrazul.medicos.dto.MedicoRequest;
import com.piedrazul.medicos.dto.MedicoResponse;
import com.piedrazul.medicos.infrastructure.persistence.MedicoRepository;
import com.piedrazul.shared.exception.BusinessException;
import com.piedrazul.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<MedicoResponse> listarActivos() {
        return medicoRepository.findByActivoTrue()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Medico obtenerPorId(Long id) {
        return medicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medico", id));
    }

    @Transactional
    public MedicoResponse crear(MedicoRequest request) {
        if (medicoRepository.existsByNumeroDocumento(request.getNumeroDocumento())) {
            throw new BusinessException(
                    "Ya existe un usuario registrado con el numero de documento: " + request.getNumeroDocumento());
        }
        Medico medico = Medico.nuevo(
                request.getNombres(),
                request.getApellidos(),
                request.getCorreo(),
                passwordEncoder.encode(request.getContrasena()),
                request.getNumeroDocumento(),
                request.getEspecialidad());
        return toResponse(medicoRepository.save(medico));
    }

    public MedicoResponse toResponse(Medico m) {
        return MedicoResponse.builder()
                .id(m.getId())
                .numeroDocumento(m.getNumeroDocumento())
                .nombres(m.getNombres())
                .apellidos(m.getApellidos())
                .correo(m.getCorreo())
                .especialidad(m.getEspecialidad())
                .activo(m.isActivo())
                .build();
    }
}
