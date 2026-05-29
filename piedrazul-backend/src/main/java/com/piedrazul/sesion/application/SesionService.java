package com.piedrazul.sesion.application;

import com.piedrazul.pacientes.application.PacienteService;
import com.piedrazul.pacientes.infrastructure.persistence.PacienteRepository;
import com.piedrazul.sesion.domain.RolUsuario;
import com.piedrazul.sesion.domain.Usuario;
import com.piedrazul.sesion.dto.LoginRequest;
import com.piedrazul.sesion.dto.UsuarioResponse;
import com.piedrazul.sesion.infrastructure.persistence.UsuarioRepository;
import com.piedrazul.shared.exception.BusinessException;
import com.piedrazul.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SesionService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final PacienteService pacienteService;

    /**
     * Valida las credenciales del usuario (médico o paciente) y retorna
     * su información básica junto con el rol para que el cliente
     * pueda redirigir a la vista correspondiente.
     *
     * Para usuarios gestionados por Keycloak (pacientes registrados via frontend),
     * la verificación de contraseña se delega a Keycloak mediante el flujo ROPC
     * que ocurre en el frontend antes de llamar a este endpoint.
     * En ese caso la contraseña almacenada es "KEYCLOAK_MANAGED" y se omite
     * la validación local con BCrypt.
     */
    @Transactional
    public UsuarioResponse iniciarSesion(LoginRequest request) {
        log.info("Intento login para documento: {}", request.getNumeroDocumento());

        Usuario usuario = usuarioRepository
                .findByNumeroDocumento(request.getNumeroDocumento())
                .orElse(null);

        if (usuario == null) {
            log.info("No encontrado en Usuario, buscando en Paciente...");
            usuario = pacienteRepository
                    .findByNumeroDocumento(request.getNumeroDocumento())
                    .orElse(null);
        }

        // Si no está en la BD local, intentar sincronizar desde Keycloak.
        // Esto ocurre cuando la BD H2 se reinicia pero el usuario ya existe en Keycloak.
        if (usuario == null) {
            log.info("No encontrado localmente, sincronizando desde Keycloak para: {}",
                    request.getNumeroDocumento());
            try {
                pacienteService.buscarPorDocumento(request.getNumeroDocumento());
                usuario = pacienteRepository
                        .findByNumeroDocumento(request.getNumeroDocumento())
                        .orElse(null);
            } catch (Exception e) {
                log.warn("Error al intentar sincronizar desde Keycloak: {}", e.getMessage());
                // Si falla la sincronización, el usuario simplemente no se encontró
            }
        }

        if (usuario == null) {
            log.warn("Usuario no encontrado ni en BD local ni en Keycloak: {}",
                    request.getNumeroDocumento());
            throw new ResourceNotFoundException("Usuario", request.getNumeroDocumento());
        }

        log.info("Usuario encontrado: {}, rol: {}", usuario.getNombres(), usuario.getRol());

        if (!usuario.isActivo()) {
            throw new BusinessException("El usuario se encuentra inactivo.");
        }

        // Para usuarios gestionados por Keycloak, la contraseña ya fue validada
        // mediante el flujo ROPC antes de llamar a este endpoint.
        if ("KEYCLOAK_MANAGED".equals(usuario.getContrasena())) {
            log.info("Usuario Keycloak-managed: omitiendo validación BCrypt local.");
        } else {
            if (!passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
                log.warn("Contraseña incorrecta para: {}", request.getNumeroDocumento());
                throw new BusinessException("Credenciales incorrectas.");
            }
        }

        return toResponse(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        return toResponse(usuario);
    }

    public UsuarioResponse toResponse(Usuario u) {
        UsuarioResponse.UsuarioResponseBuilder builder = UsuarioResponse.builder()
                .id(u.getId())
                .numeroDocumento(u.getNumeroDocumento())
                .nombres(u.getNombres())
                .apellidos(u.getApellidos())
                .correo(u.getCorreo())
                .rol(u.getRol())
                .activo(u.isActivo());

        if (u.getRol() == RolUsuario.PACIENTE) {
            pacienteRepository.findByNumeroDocumento(u.getNumeroDocumento())
                    .ifPresent(p -> {
                        builder.celular(p.getCelular());
                        builder.genero(p.getGenero() != null ? p.getGenero().name() : null);
                    });
        }

        return builder.build();
    }
}
