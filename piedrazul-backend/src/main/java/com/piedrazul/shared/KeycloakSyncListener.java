package com.piedrazul.shared;

import com.piedrazul.pacientes.domain.Paciente;
import com.piedrazul.pacientes.infrastructure.persistence.PacienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakSyncListener {

    private final PacienteRepository pacienteRepository;

    @EventListener
    @Transactional
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        var auth = event.getAuthentication();

        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) return;
        if (!hasRole(jwtAuth, "PACIENTE")) return;

        Map<String, Object> claims = jwtAuth.getToken().getClaims();
        String docNumber = (String) claims.get("preferred_username");
        if (docNumber == null || docNumber.isBlank()) return;

        if (pacienteRepository.findByNumeroDocumento(docNumber).isPresent()) return;

        String nombres = (String) claims.getOrDefault("given_name", "");
        String apellidos = (String) claims.getOrDefault("family_name", "");
        String email = (String) claims.getOrDefault("email", docNumber + "@sin-correo.com");

        Paciente paciente = Paciente.nuevo(
                nombres,
                apellidos,
                email,
                "KEYCLOAK_MANAGED",
                docNumber,
                null,
                null,
                null
        );

        pacienteRepository.save(paciente);
        log.info("Paciente {} sincronizado desde Keycloak a BD local", docNumber);
    }

    private boolean hasRole(JwtAuthenticationToken auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(g -> g.getAuthority().equals("ROLE_" + role));
    }
}
