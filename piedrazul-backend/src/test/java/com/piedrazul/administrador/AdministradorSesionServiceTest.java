package com.piedrazul.administrador;

import com.piedrazul.pacientes.infrastructure.persistence.PacienteRepository;
import com.piedrazul.sesion.application.SesionService;
import com.piedrazul.sesion.domain.RolUsuario;
import com.piedrazul.sesion.domain.Usuario;
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
@DisplayName("Administrador - SesionService - pruebas unitarias")
class AdministradorSesionServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private SesionService sesionService;

    private static final String HASH = "$2a$10$hashedAdminPassword";

    private Usuario admin;
    private LoginRequest loginAdmin;

    @BeforeEach
    void setUp() {
        admin = new Usuario();
        admin.setId(1L);
        admin.setNombres("Administrador");
        admin.setApellidos("Piedrazul");
        admin.setNumeroDocumento("admin");
        admin.setContrasena(HASH);
        admin.setRol(RolUsuario.ADMIN);
        admin.setActivo(true);

        loginAdmin = new LoginRequest();
        loginAdmin.setNumeroDocumento("admin");
        loginAdmin.setContrasena("admin1234");
    }

    // ── iniciarSesion ────────────────────────────────────────────────────────

    @Test
    @DisplayName("iniciarSesion - admin con credenciales correctas - retorna response con rol ADMIN")
    void iniciarSesion_adminCredencialesCorrectas_retornaRolAdmin() {
        when(usuarioRepository.findByNumeroDocumento("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("admin1234", HASH)).thenReturn(true);

        UsuarioResponse response = sesionService.iniciarSesion(loginAdmin);

        assertThat(response.getRol()).isEqualTo(RolUsuario.ADMIN);
        assertThat(response.getNombres()).isEqualTo("Administrador");
        assertThat(response.getApellidos()).isEqualTo("Piedrazul");
        assertThat(response.getNumeroDocumento()).isEqualTo("admin");
        assertThat(response.isActivo()).isTrue();
    }

    @Test
    @DisplayName("iniciarSesion - admin con credenciales correctas - no consulta tabla pacientes")
    void iniciarSesion_adminEncontradoEnUsuarios_noConsultaPacientes() {
        when(usuarioRepository.findByNumeroDocumento("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("admin1234", HASH)).thenReturn(true);

        sesionService.iniciarSesion(loginAdmin);

        verify(pacienteRepository, never()).findByNumeroDocumento(anyString());
    }

    @Test
    @DisplayName("iniciarSesion - documento admin no registrado - lanza ResourceNotFoundException")
    void iniciarSesion_documentoAdminNoRegistrado_lanzaResourceNotFoundException() {
        when(usuarioRepository.findByNumeroDocumento("admin")).thenReturn(Optional.empty());
        when(pacienteRepository.findByNumeroDocumento("admin")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sesionService.iniciarSesion(loginAdmin))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("iniciarSesion - contrasena incorrecta para admin - lanza BusinessException")
    void iniciarSesion_contrasenaIncorrecta_lanzaBusinessException() {
        when(usuarioRepository.findByNumeroDocumento("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrong_pass", HASH)).thenReturn(false);

        loginAdmin.setContrasena("wrong_pass");

        assertThatThrownBy(() -> sesionService.iniciarSesion(loginAdmin))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Credenciales incorrectas");
    }

    @Test
    @DisplayName("iniciarSesion - admin inactivo - lanza BusinessException sin verificar contrasena")
    void iniciarSesion_adminInactivo_lanzaBusinessExceptionSinVerificarHash() {
        admin.setActivo(false);
        when(usuarioRepository.findByNumeroDocumento("admin")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> sesionService.iniciarSesion(loginAdmin))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inactivo");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    // ── obtenerPorId ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerPorId - admin existente - retorna response con datos correctos")
    void obtenerPorId_adminExistente_retornaResponse() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        UsuarioResponse response = sesionService.obtenerPorId(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRol()).isEqualTo(RolUsuario.ADMIN);
        assertThat(response.getNombres()).isEqualTo("Administrador");
    }

    @Test
    @DisplayName("obtenerPorId - id no existente - lanza ResourceNotFoundException")
    void obtenerPorId_noExistente_lanzaException() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sesionService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── toResponse ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("toResponse - usuario con rol ADMIN - no enriquece con datos de paciente")
    void toResponse_rolAdmin_noConsultaPacienteRepository() {
        UsuarioResponse response = sesionService.toResponse(admin);

        assertThat(response.getRol()).isEqualTo(RolUsuario.ADMIN);
        assertThat(response.getCelular()).isNull();
        assertThat(response.getGenero()).isNull();
        verify(pacienteRepository, never()).findByNumeroDocumento(anyString());
    }

    @Test
    @DisplayName("toResponse - admin - todos los campos base estan presentes")
    void toResponse_admin_camposBasePresentes() {
        UsuarioResponse response = sesionService.toResponse(admin);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNumeroDocumento()).isEqualTo("admin");
        assertThat(response.getNombres()).isEqualTo("Administrador");
        assertThat(response.getApellidos()).isEqualTo("Piedrazul");
        assertThat(response.getRol()).isEqualTo(RolUsuario.ADMIN);
        assertThat(response.isActivo()).isTrue();
    }
}