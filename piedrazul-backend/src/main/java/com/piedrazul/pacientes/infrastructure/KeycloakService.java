package com.piedrazul.pacientes.infrastructure;

import com.piedrazul.pacientes.dto.PacienteRequest;
import com.piedrazul.shared.exception.BusinessException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class KeycloakService {

    private final String serverUrl;
    private final String realm;
    private final String clientId;
    private final String adminUsername;
    private final String adminPassword;

    public KeycloakService(
            @Value("${keycloak.server-url}") String serverUrl,
            @Value("${keycloak.realm}") String realm,
            @Value("${keycloak.admin.client-id}") String clientId,
            @Value("${keycloak.admin.username}") String adminUsername,
            @Value("${keycloak.admin.password}") String adminPassword) {
        this.serverUrl = serverUrl;
        this.realm = realm;
        this.clientId = clientId;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    private Keycloak getInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master") // Autenticarse contra master para administrar
                .clientId(clientId)
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }

    public void crearUsuario(PacienteRequest req) {
        try (Keycloak keycloak = getInstance()) {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Verificar si el usuario ya existe
            List<UserRepresentation> existingUsers = usersResource.search(req.getNumeroDocumento());
            if (!existingUsers.isEmpty()) {
                log.warn("El usuario {} ya existe en Keycloak", req.getNumeroDocumento());
                return; // Si existe, no lo volvemos a crear (idempotencia)
            }

            // Crear el usuario
            UserRepresentation user = new UserRepresentation();
            user.setEnabled(true);
            user.setUsername(req.getNumeroDocumento());
            user.setFirstName(req.getNombres());
            user.setLastName(req.getApellidos());
            user.setEmail(req.getCorreo());
            user.setEmailVerified(true);

            // Asignar credencial (contraseña)
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setTemporary(true); // El paciente deberá cambiarla al primer inicio
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(req.getNumeroDocumento()); // Contraseña por defecto es el documento
            user.setCredentials(Collections.singletonList(credential));

            // Llamar a la API para crear
            Response response = usersResource.create(user);
            if (response.getStatus() == 201) {
                log.info("Usuario {} creado exitosamente en Keycloak", req.getNumeroDocumento());

                // Obtener ID del usuario recién creado para asignarle roles
                String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

                // Asignar rol PACIENTE
                asignarRolPaciente(realmResource, userId);
            } else if (response.getStatus() == 409) {
                log.warn("Conflicto al crear usuario {} en Keycloak (ya existe)", req.getNumeroDocumento());
            } else {
                log.error("Error al crear usuario en Keycloak. HTTP Status: {}", response.getStatus());
                throw new BusinessException("Error de integración: no se pudo registrar el usuario en el sistema de autenticación.");
            }
        } catch (Exception e) {
            log.error("Excepción al intentar crear el usuario en Keycloak", e);
            throw new BusinessException("No fue posible comunicarse con el servicio de autenticación.");
        }
    }

    private void asignarRolPaciente(RealmResource realmResource, String userId) {
        try {
            RoleRepresentation pacienteRole = realmResource.roles().get("PACIENTE").toRepresentation();
            UserResource userResource = realmResource.users().get(userId);
            userResource.roles().realmLevel().add(Collections.singletonList(pacienteRole));
            log.info("Rol PACIENTE asignado al usuario con ID {}", userId);
        } catch (Exception e) {
            log.error("Error al asignar rol PACIENTE al usuario {}", userId, e);
            throw new BusinessException("El usuario se creó, pero falló la asignación de roles.");
        }
    }
}
