package com.piedrazul.agendadores.application;

import com.piedrazul.agendadores.domain.Agendador;
import com.piedrazul.agendadores.dto.AgendadorRequest;
import com.piedrazul.agendadores.dto.AgendadorResponse;
import com.piedrazul.agendadores.infrastructure.persistence.AgendadorRepository;
import com.piedrazul.pacientes.infrastructure.KeycloakService;
import com.piedrazul.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgendadorService {

    private final AgendadorRepository agendadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final KeycloakService keycloakService;

    @Transactional
    public AgendadorResponse crear(AgendadorRequest request) {
        if (agendadorRepository.findByNumeroDocumento(request.getNumeroDocumento()).isPresent()) {
            throw new BusinessException("Ya existe un agendador registrado con el documento "
                    + request.getNumeroDocumento());
        }

        String encodedPassword = passwordEncoder.encode(request.getNumeroDocumento());

        Agendador agendador = Agendador.nuevo(
                request.getNombres(),
                request.getApellidos(),
                request.getCorreo(),
                encodedPassword,
                request.getNumeroDocumento(),
                request.getCelular(),
                request.getGenero(),
                request.getFechaNacimiento()
        );

        Agendador saved = agendadorRepository.save(agendador);

        keycloakService.crearAgendador(request);

        return toResponse(saved);
    }

    private AgendadorResponse toResponse(Agendador a) {
        return AgendadorResponse.builder()
                .id(a.getId())
                .numeroDocumento(a.getNumeroDocumento())
                .nombres(a.getNombres())
                .apellidos(a.getApellidos())
                .correo(a.getCorreo())
                .celular(a.getCelular())
                .genero(a.getGenero())
                .fechaNacimiento(a.getFechaNacimiento())
                .build();
    }
}
