package com.piedrazul.usuarios.application;

import com.piedrazul.shared.config.JwtConfig;
import com.piedrazul.usuarios.domain.Rol;
import com.piedrazul.usuarios.domain.Usuario;
import com.piedrazul.usuarios.dto.LoginRequest;
import com.piedrazul.usuarios.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtConfig jwtConfig;
    @Mock private UsuarioService usuarioService;
    @InjectMocks private AuthService authService;

    private Usuario usuario;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L).username("agendador1")
                .password("encoded").rol(Rol.AGENDADOR).build();
        loginRequest = new LoginRequest();
        loginRequest.setUsername("agendador1");
        loginRequest.setPassword("password123");
    }

    @Test
    void login_credencialesValidas_retornaTokenYRol() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(usuario);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtConfig.generateToken(any(Usuario.class), anyMap())).thenReturn("jwt-token-mock");

        LoginResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("jwt-token-mock");
        assertThat(response.getRol()).isEqualTo(Rol.AGENDADOR);
        assertThat(response.getUsername()).isEqualTo("agendador1");
    }

    @Test
    void login_credencialesInvalidas_lanzaBadCredentialsException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }
}
