package com.piedrazul.sesion.application;

import com.piedrazul.sesion.domain.Usuario;
import com.piedrazul.sesion.dto.LoginRequest;
import com.piedrazul.sesion.dto.UsuarioResponse;
import com.piedrazul.sesion.infrastructure.persistence.UsuarioRepository;
import com.piedrazul.shared.exception.BusinessException;
import com.piedrazul.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SesionService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Valida las credenciales del usuario (médico o paciente) y retorna
     * su información básica junto con el rol para que el cliente
     * pueda redirigir a la vista correspondiente.
     * La contraseña almacenada está hasheada con BCrypt.
     */
    @Transactional(readOnly = true)
    public UsuarioResponse iniciarSesion(LoginRequest request) {
        Usuario usuario = usuarioRepository
                .findByCorreo(request.getCorreo())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario", request.getCorreo()));

        if (!usuario.isActivo()) {
            throw new BusinessException("El usuario se encuentra inactivo.");
        }

        if (!passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
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
        return UsuarioResponse.builder()
                .id(u.getId())
                .nombres(u.getNombres())
                .apellidos(u.getApellidos())
                .correo(u.getCorreo())
                .rol(u.getRol())
                .activo(u.isActivo())
                .build();
    }
}
