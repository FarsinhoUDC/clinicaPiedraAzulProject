package com.piedrazul.pacientes.application;

import com.piedrazul.pacientes.domain.Genero;
import com.piedrazul.pacientes.domain.Paciente;
import com.piedrazul.pacientes.dto.PacienteRequest;
import com.piedrazul.pacientes.dto.PacienteResponse;
import com.piedrazul.pacientes.infrastructure.persistence.PacienteRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PacienteService - pruebas unitarias")
class PacienteServiceTest {

    @Mock private PacienteRepository pacienteRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private PacienteService pacienteService;

    private Paciente paciente;
    private PacienteRequest request;

    /** Construye un Paciente válido con contraseña ya hasheada. */
    private Paciente pacienteValido() {
        return Paciente.nuevo(
                "Maria", "Lopez",
                "maria.lopez@test.com", "$2a$10$hashedpassword",
                "12345", "3001234567",
                Genero.MUJER, null);
    }

    @BeforeEach
    void setUp() {
        paciente = pacienteValido();
        paciente.setId(1L);

        request = new PacienteRequest();
        request.setNumeroDocumento("12345");
        request.setNombres("Maria");
        request.setApellidos("Lopez");
        request.setCorreo("maria.lopez@test.com");
        request.setContrasena("pass123");
        request.setCelular("3001234567");
        request.setGenero(Genero.MUJER);
    }

    @Test
    @DisplayName("crearOActualizar - paciente nuevo - hashea contrasena y guarda")
    void crearOActualizar_pacienteNuevo_hashea_y_guarda() {
        when(pacienteRepository.findByNumeroDocumento("12345")).thenReturn(Optional.empty());
        when(pacienteRepository.existsByCorreo("maria.lopez@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("$2a$10$hashedpassword");
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);

        PacienteResponse resultado = pacienteService.crearOActualizar(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNumeroDocumento()).isEqualTo("12345");
        assertThat(resultado.getCorreo()).isEqualTo("maria.lopez@test.com");
        verify(passwordEncoder).encode("pass123");
        verify(pacienteRepository).save(any(Paciente.class));
    }

    @Test
    @DisplayName("crearOActualizar - correo duplicado en paciente nuevo - lanza BusinessException")
    void crearOActualizar_correoDuplicado_lanzaBusinessException() {
        when(pacienteRepository.findByNumeroDocumento("12345")).thenReturn(Optional.empty());
        when(pacienteRepository.existsByCorreo("maria.lopez@test.com")).thenReturn(true);

        assertThatThrownBy(() -> pacienteService.crearOActualizar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("correo");

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("crearOActualizar - paciente existente - actualiza sin re-hashear")
    void crearOActualizar_pacienteExistente_actualizaSinRehashear() {
        when(pacienteRepository.findByNumeroDocumento("12345")).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);

        request.setNombres("Maria Actualizada");
        PacienteResponse resultado = pacienteService.crearOActualizar(request);

        assertThat(resultado.getNombres()).isEqualTo("Maria Actualizada");
        verify(passwordEncoder, never()).encode(anyString());
        verify(pacienteRepository, never()).existsByCorreo(anyString());
    }

    @Test
    @DisplayName("buscarPorDocumento - existente - retorna Optional con valor")
    void buscarPorDocumento_existente_retornaOptionalConValor() {
        when(pacienteRepository.findByNumeroDocumento("12345")).thenReturn(Optional.of(paciente));

        Optional<PacienteResponse> resultado = pacienteService.buscarPorDocumento("12345");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombres()).isEqualTo("Maria");
    }

    @Test
    @DisplayName("buscarPorDocumento - no existente - retorna Optional vacio")
    void buscarPorDocumento_noExistente_retornaOptionalVacio() {
        when(pacienteRepository.findByNumeroDocumento("99999")).thenReturn(Optional.empty());

        Optional<PacienteResponse> resultado = pacienteService.buscarPorDocumento("99999");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("obtenerEntidadPorDocumento - no existente - lanza ResourceNotFoundException")
    void obtenerEntidadPorDocumento_noExistente_lanzaException() {
        when(pacienteRepository.findByNumeroDocumento("99999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteService.obtenerEntidadPorDocumento("99999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
