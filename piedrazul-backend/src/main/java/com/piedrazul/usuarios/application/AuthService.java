package com.piedrazul.usuarios.application;

import com.piedrazul.shared.config.JwtConfig;
import com.piedrazul.usuarios.domain.Usuario;
import com.piedrazul.usuarios.dto.LoginRequest;
import com.piedrazul.usuarios.dto.LoginResponse;
import com.piedrazul.usuarios.dto.RegistroUsuarioRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtConfig jwtConfig;
    private final UsuarioService usuarioService;

    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        Usuario usuario = (Usuario) auth.getPrincipal();
        String token = jwtConfig.generateToken(usuario, Map.of("rol", usuario.getRol().name()));
        return LoginResponse.builder().token(token)
                .username(usuario.getUsername()).rol(usuario.getRol()).build();
    }

    public LoginResponse registrarYLogin(RegistroUsuarioRequest request) {
        usuarioService.registrar(request);
        LoginRequest lr = new LoginRequest();
        lr.setUsername(request.getUsername());
        lr.setPassword(request.getPassword());
        return login(lr);
    }
}
