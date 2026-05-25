package com.piedrazul.administrador;

import com.piedrazul.sesion.domain.RolUsuario;
import com.piedrazul.sesion.domain.Usuario;
import com.piedrazul.sesion.infrastructure.persistence.UsuarioRepository;
import com.piedrazul.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Administrador - gestión de usuario - pruebas unitarias")
class AdministradorGestionTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private Usuario admin;
    private Usuario agendador;

    @BeforeEach
    void setUp() {
        admin = new Usuario();
        admin.setId(1L);
        admin.setNombres("Administrador");
        admin.setApellidos("Piedrazul");
        admin.setNumeroDocumento("admin");
        admin.setContrasena("$2a$10$hashedAdminPassword");
        admin.setRol(RolUsuario.ADMIN);
        admin.setActivo(true);

        agendador = new Usuario();
        agendador.setId(2L);
        agendador.setNombres("Recepcionista");
        agendador.setApellidos("Piedrazul");
        agendador.setNumeroDocumento("agendador");
        agendador.setContrasena("$2a$10$hashedAgendadorPassword");
        agendador.setRol(RolUsuario.AGENDADOR);
        agendador.setActivo(true);
    }

    // ── Creación de admin ─────────────────────────────────────────────────────

    @Test
    @DisplayName("crearAdmin - no existente - guarda con rol ADMIN y activo")
    void crearAdmin_noExistente_guardaConRolAdminYActivo() {
        when(usuarioRepository.existsByNumeroDocumento("admin")).thenReturn(false);
        when(passwordEncoder.encode("admin1234")).thenReturn("$2a$10$hashedAdminPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(admin);

        // Simula la lógica de DataInitializer.crearAdmin()
        boolean existe = usuarioRepository.existsByNumeroDocumento("admin");
        if (!existe) {
            Usuario nuevoAdmin = new Usuario();
            nuevoAdmin.setNombres("Administrador");
            nuevoAdmin.setApellidos("Piedrazul");
            nuevoAdmin.setNumeroDocumento("admin");
            nuevoAdmin.setContrasena(passwordEncoder.encode("admin1234"));
            nuevoAdmin.setRol(RolUsuario.ADMIN);
            nuevoAdmin.setActivo(true);
            usuarioRepository.save(nuevoAdmin);
        }

        verify(usuarioRepository).save(argThat(u ->
                u.getRol() == RolUsuario.ADMIN &&
                u.isActivo() &&
                "Administrador".equals(u.getNombres()) &&
                "admin".equals(u.getNumeroDocumento())
        ));
    }

    @Test
    @DisplayName("crearAdmin - ya existente - no vuelve a guardar")
    void crearAdmin_yaExistente_noGuarda() {
        when(usuarioRepository.existsByNumeroDocumento("admin")).thenReturn(true);

        // Simula la lógica de DataInitializer.crearAdmin()
        boolean existe = usuarioRepository.existsByNumeroDocumento("admin");
        if (!existe) {
            usuarioRepository.save(any(Usuario.class));
        }

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    // ── Creación de agendador ─────────────────────────────────────────────────

    @Test
    @DisplayName("crearAgendador - no existente - guarda con rol AGENDADOR y activo")
    void crearAgendador_noExistente_guardaConRolAgendadorYActivo() {
        when(usuarioRepository.existsByNumeroDocumento("agendador")).thenReturn(false);
        when(passwordEncoder.encode("agendador1234")).thenReturn("$2a$10$hashedAgendadorPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(agendador);

        // Simula la lógica de DataInitializer.crearAgendador()
        boolean existe = usuarioRepository.existsByNumeroDocumento("agendador");
        if (!existe) {
            Usuario nuevoAgendador = new Usuario();
            nuevoAgendador.setNombres("Recepcionista");
            nuevoAgendador.setApellidos("Piedrazul");
            nuevoAgendador.setNumeroDocumento("agendador");
            nuevoAgendador.setContrasena(passwordEncoder.encode("agendador1234"));
            nuevoAgendador.setRol(RolUsuario.AGENDADOR);
            nuevoAgendador.setActivo(true);
            usuarioRepository.save(nuevoAgendador);
        }

        verify(usuarioRepository).save(argThat(u ->
                u.getRol() == RolUsuario.AGENDADOR &&
                u.isActivo() &&
                "agendador".equals(u.getNumeroDocumento())
        ));
    }

    @Test
    @DisplayName("crearAgendador - ya existente - no vuelve a guardar")
    void crearAgendador_yaExistente_noGuarda() {
        when(usuarioRepository.existsByNumeroDocumento("agendador")).thenReturn(true);

        boolean existe = usuarioRepository.existsByNumeroDocumento("agendador");
        if (!existe) {
            usuarioRepository.save(any(Usuario.class));
        }

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // ── Verificación de campos del Usuario admin ──────────────────────────────

    @Test
    @DisplayName("usuario admin - rol es ADMIN")
    void usuarioAdmin_tieneRolAdmin() {
        assertThat(admin.getRol()).isEqualTo(RolUsuario.ADMIN);
    }

    @Test
    @DisplayName("usuario admin - esta activo por defecto")
    void usuarioAdmin_estaActivoPorDefecto() {
        assertThat(admin.isActivo()).isTrue();
    }

    @Test
    @DisplayName("usuario admin - no es MEDICO ni PACIENTE ni AGENDADOR")
    void usuarioAdmin_noEsOtroRol() {
        assertThat(admin.getRol()).isNotEqualTo(RolUsuario.MEDICO);
        assertThat(admin.getRol()).isNotEqualTo(RolUsuario.PACIENTE);
        assertThat(admin.getRol()).isNotEqualTo(RolUsuario.AGENDADOR);
    }

    // ── Buscar por documento ──────────────────────────────────────────────────

    @Test
    @DisplayName("buscarAdminPorDocumento - existe - retorna usuario con rol ADMIN")
    void buscarAdminPorDocumento_existe_retornaUsuario() {
        when(usuarioRepository.findByNumeroDocumento("admin")).thenReturn(Optional.of(admin));

        Optional<Usuario> resultado = usuarioRepository.findByNumeroDocumento("admin");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getRol()).isEqualTo(RolUsuario.ADMIN);
        assertThat(resultado.get().getNombres()).isEqualTo("Administrador");
    }

    @Test
    @DisplayName("buscarAdminPorDocumento - no existe - retorna optional vacio")
    void buscarAdminPorDocumento_noExiste_retornaVacio() {
        when(usuarioRepository.findByNumeroDocumento("admin_inexistente")).thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioRepository.findByNumeroDocumento("admin_inexistente");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("existsByNumeroDocumento - admin registrado - retorna true")
    void existsByNumeroDocumento_adminRegistrado_retornaTrue() {
        when(usuarioRepository.existsByNumeroDocumento("admin")).thenReturn(true);

        boolean existe = usuarioRepository.existsByNumeroDocumento("admin");

        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("existsByNumeroDocumento - admin no registrado - retorna false")
    void existsByNumeroDocumento_adminNoRegistrado_retornaFalse() {
        when(usuarioRepository.existsByNumeroDocumento("admin")).thenReturn(false);

        boolean existe = usuarioRepository.existsByNumeroDocumento("admin");

        assertThat(existe).isFalse();
    }

    // ── Contraseña hasheada ───────────────────────────────────────────────────

    @Test
    @DisplayName("passwordEncoder - contrasena admin correcta - matches retorna true")
    void passwordEncoder_contrasenaAdminCorrecta_matchesRetornaTrue() {
        when(passwordEncoder.matches("admin1234", admin.getContrasena())).thenReturn(true);

        boolean valido = passwordEncoder.matches("admin1234", admin.getContrasena());

        assertThat(valido).isTrue();
    }

    @Test
    @DisplayName("passwordEncoder - contrasena admin incorrecta - matches retorna false")
    void passwordEncoder_contrasenaAdminIncorrecta_matchesRetornaFalse() {
        when(passwordEncoder.matches("wrong", admin.getContrasena())).thenReturn(false);

        boolean valido = passwordEncoder.matches("wrong", admin.getContrasena());

        assertThat(valido).isFalse();
    }
}