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
import com.piedrazul.medicos.dto.MedicoRequest;
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
            
            // Asignar un correo ficticio si el paciente no lo proporciona, 
            // para evitar que Keycloak exija el campo "Update Account Information"
            String email = (req.getCorreo() != null && !req.getCorreo().trim().isEmpty()) 
                    ? req.getCorreo() 
                    : req.getNumeroDocumento() + "@sin-correo.com";
            user.setEmail(email);
            user.setEmailVerified(true);

            // Asignar credencial (contraseña)
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setTemporary(false); // La contraseña es definitiva, no requiere cambio al primer inicio
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

    public void crearMedico(MedicoRequest req) {
        try (Keycloak keycloak = getInstance()) {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Verificar si el usuario ya existe
            List<UserRepresentation> existingUsers = usersResource.search(req.getNumeroDocumento());
            if (!existingUsers.isEmpty()) {
                log.warn("El medico {} ya existe en Keycloak", req.getNumeroDocumento());
                return;
            }

            // Crear el usuario
            UserRepresentation user = new UserRepresentation();
            user.setEnabled(true);
            user.setUsername(req.getNumeroDocumento());
            user.setFirstName(req.getNombres());
            user.setLastName(req.getApellidos());
            
            String email = (req.getCorreo() != null && !req.getCorreo().trim().isEmpty()) 
                    ? req.getCorreo() 
                    : req.getNumeroDocumento() + "@sin-correo.com";
            user.setEmail(email);
            user.setEmailVerified(true);

            // Asignar credencial (contraseña)
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setTemporary(false);
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(req.getNumeroDocumento());
            user.setCredentials(Collections.singletonList(credential));

            Response response = usersResource.create(user);
            if (response.getStatus() == 201) {
                log.info("Medico {} creado exitosamente en Keycloak", req.getNumeroDocumento());

                String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

                asignarRolMedico(realmResource, userId);
            } else if (response.getStatus() == 409) {
                log.warn("Conflicto al crear medico {} en Keycloak (ya existe)", req.getNumeroDocumento());
            } else {
                log.error("Error al crear medico en Keycloak. HTTP Status: {}", response.getStatus());
                throw new BusinessException("Error de integración: no se pudo registrar el medico en el sistema de autenticación.");
            }
        } catch (Exception e) {
            log.error("Excepción al intentar crear el medico en Keycloak", e);
            throw new BusinessException("No fue posible comunicarse con el servicio de autenticación.");
        }
    }

    private void asignarRolMedico(RealmResource realmResource, String userId) {
        try {
            RoleRepresentation medicoRole = realmResource.roles().get("MEDICO").toRepresentation();
            UserResource userResource = realmResource.users().get(userId);
            userResource.roles().realmLevel().add(Collections.singletonList(medicoRole));
            log.info("Rol MEDICO asignado al usuario con ID {}", userId);
        } catch (Exception e) {
            log.error("Error al asignar rol MEDICO al usuario {}", userId, e);
            throw new BusinessException("El medico se creó, pero falló la asignación de roles.");
        }
    }
}
