package com.piedrazul.sesion.application;

import com.piedrazul.medicos.domain.Medico;
import com.piedrazul.pacientes.domain.Genero;
import com.piedrazul.pacientes.domain.Paciente;
import com.piedrazul.sesion.domain.RolUsuario;
import com.piedrazul.sesion.dto.LoginRequest;
import com.piedrazul.sesion.dto.UsuarioResponse;
import com.piedrazul.sesion.infrastructure.persistence.UsuarioRepository;
import com.piedrazul.shared.exception.BusinessException;
import com.piedrazul.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SesionService - pruebas unitarias")
class SesionServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private SesionService sesionService;

    private static final String HASH = "$2a$10$hashedpassword";

    private Medico medico;
    private Paciente paciente;
    private LoginRequest loginMedico;
    private LoginRequest loginPaciente;

    @BeforeEach
    void setUp() {
        medico = Medico.nuevo("Carlos", "Gomez", "carlos@test.com", HASH, "1234", "Medicina General");
        medico.setId(1L);

        paciente = Paciente.nuevo("Maria", "Lopez", "maria@test.com", HASH,
                "12345", "3001234567", Genero.MUJER, null);
        paciente.setId(2L);

        loginMedico = new LoginRequest();
        loginMedico.setNumeroDocumento("1234");
        loginMedico.setContrasena("clave123");

        loginPaciente = new LoginRequest();
        loginPaciente.setNumeroDocumento("12345");
        loginPaciente.setContrasena("clave456");
    }

    @Test
    @DisplayName("iniciarSesion - medico con credenciales correctas - retorna response con rol MEDICO")
    void iniciarSesion_medicoCredencialesCorrectas_retornaRolMedico() {
        when(usuarioRepository.findByNumeroDocumento("1234")).thenReturn(Optional.of(medico));
        when(passwordEncoder.matches("clave123", HASH)).thenReturn(true);

        UsuarioResponse response = sesionService.iniciarSesion(loginMedico);

        assertThat(response.getRol()).isEqualTo(RolUsuario.MEDICO);
        assertThat(response.getNombres()).isEqualTo("Carlos");
    }

    @Test
    @DisplayName("iniciarSesion - paciente con credenciales correctas - retorna response con rol PACIENTE")
    void iniciarSesion_pacienteCredencialesCorrectas_retornaRolPaciente() {
        when(usuarioRepository.findByNumeroDocumento("12345")).thenReturn(Optional.of(paciente));
        when(passwordEncoder.matches("clave456", HASH)).thenReturn(true);

        UsuarioResponse response = sesionService.iniciarSesion(loginPaciente);

        assertThat(response.getRol()).isEqualTo(RolUsuario.PACIENTE);
        assertThat(response.getNombres()).isEqualTo("Maria");
    }

    @Test
    @DisplayName("iniciarSesion - numero de documento no registrado - lanza ResourceNotFoundException")
    void iniciarSesion_documentoNoRegistrado_lanzaResourceNotFoundException() {
        when(usuarioRepository.findByNumeroDocumento("99999")).thenReturn(Optional.empty());

        LoginRequest req = new LoginRequest();
        req.setNumeroDocumento("99999");
        req.setContrasena("cualquier");

        assertThatThrownBy(() -> sesionService.iniciarSesion(req))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("iniciarSesion - contrasena incorrecta - lanza BusinessException")
    void iniciarSesion_contrasenaIncorrecta_lanzaBusinessException() {
        when(usuarioRepository.findByNumeroDocumento("1234")).thenReturn(Optional.of(medico));
        when(passwordEncoder.matches("WRONG", HASH)).thenReturn(false);

        loginMedico.setContrasena("WRONG");

        assertThatThrownBy(() -> sesionService.iniciarSesion(loginMedico))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Credenciales incorrectas");
    }

    @Test
    @DisplayName("iniciarSesion - usuario inactivo - lanza BusinessException antes de verificar contrasena")
    void iniciarSesion_usuarioInactivo_lanzaBusinessExceptionSinVerificarHash() {
        medico.setActivo(false);
        when(usuarioRepository.findByNumeroDocumento("1234")).thenReturn(Optional.of(medico));

        assertThatThrownBy(() -> sesionService.iniciarSesion(loginMedico))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inactivo");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("obtenerPorId - id existente - retorna response correcto")
    void obtenerPorId_idExistente_retornaResponse() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(medico));

        UsuarioResponse response = sesionService.obtenerPorId(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRol()).isEqualTo(RolUsuario.MEDICO);
    }

    @Test
    @DisplayName("obtenerPorId - id no existente - lanza ResourceNotFoundException")
    void obtenerPorId_noExistente_lanzaException() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sesionService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}