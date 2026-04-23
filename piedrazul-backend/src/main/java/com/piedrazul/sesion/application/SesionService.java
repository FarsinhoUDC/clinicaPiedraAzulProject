package com.piedrazul.sesion.application;

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

    /**
     * Valida las credenciales del usuario (médico o paciente) y retorna
     * su información básica junto con el rol para que el cliente
     * pueda redirigir a la vista correspondiente.
     * La contraseña almacenada está hasheada con BCrypt.
     */
    @Transactional(readOnly = true)
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

        if (usuario == null) {
            log.warn("Usuario no encontrado: {}", request.getNumeroDocumento());
            throw new ResourceNotFoundException(
                    "Usuario", request.getNumeroDocumento());
        }

        log.info("Usuario encontrado: {}, rol: {}", usuario.getNombres(), usuario.getRol());

        if (!usuario.isActivo()) {
            throw new BusinessException("El usuario se encuentra inactivo.");
        }

        if (!passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
            log.warn("Contraseña incorrecta para: {}", request.getNumeroDocumento());
            throw new BusinessException("Credenciales incorrectas.");
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
                        builder.genero(p.getGenero().name());
                    });
        }

        return builder.build();
    }
}
