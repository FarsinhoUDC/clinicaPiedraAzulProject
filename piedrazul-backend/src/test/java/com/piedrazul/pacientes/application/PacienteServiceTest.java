package com.piedrazul.pacientes.application;

import com.piedrazul.pacientes.domain.Genero;
import com.piedrazul.pacientes.domain.Paciente;
import com.piedrazul.pacientes.dto.PacienteRequest;
import com.piedrazul.pacientes.dto.PacienteResponse;
import com.piedrazul.pacientes.infrastructure.persistence.PacienteRepository;
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
@DisplayName("PacienteService - pruebas unitarias")
class PacienteServiceTest {

    @Mock private PacienteRepository pacienteRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private PacienteService pacienteService;

    private Paciente paciente;
    private PacienteRequest request;

    @BeforeEach
    void setUp() {
        paciente = Paciente.nuevo("Maria", "Lopez", "maria@test.com", "hashedpass",
                "12345", "3001234567", Genero.MUJER, null);
        paciente.setId(1L);

        request = new PacienteRequest();
        request.setNumeroDocumento("12345");
        request.setNombres("Maria");
        request.setApellidos("Lopez");
        request.setCorreo("maria@test.com");
        request.setContrasena("pass123");
        request.setCelular("3001234567");
        request.setGenero(Genero.MUJER);
    }

    @Test
    @DisplayName("crearOActualizar - nuevo paciente - guarda y retorna response")
    void crearOActualizar_nuevoPaciente_guardaYRetorna() {
        when(pacienteRepository.findByNumeroDocumento("12345")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("hashedpass");
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);

        PacienteResponse response = pacienteService.crearOActualizar(request);

        assertThat(response).isNotNull();
        assertThat(response.getNombres()).isEqualTo("Maria");
        verify(pacienteRepository).save(any(Paciente.class));
    }

    @Test
    @DisplayName("crearOActualizar - paciente existente - actualiza solo campos propios")
    void crearOActualizar_existente_actualizaCampos() {
        when(pacienteRepository.findByNumeroDocumento("12345")).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);

        request.setNombres("Maria Actualizada");
        PacienteResponse response = pacienteService.crearOActualizar(request);

        assertThat(response.getNombres()).isEqualTo("Maria Actualizada");
    }

    @Test
    @DisplayName("buscarPorDocumento - existe - retorna optional con paciente")
    void buscarPorDocumento_existe_retornaOptional() {
        when(pacienteRepository.findByNumeroDocumento("12345")).thenReturn(Optional.of(paciente));

        var result = pacienteService.buscarPorDocumento("12345");

        assertThat(result).isPresent();
        assertThat(result.get().getNombres()).isEqualTo("Maria");
    }

    @Test
    @DisplayName("buscarPorDocumento - no existe - retorna optional vacio")
    void buscarPorDocumento_noExiste_retornaVacio() {
        when(pacienteRepository.findByNumeroDocumento("99999")).thenReturn(Optional.empty());

        var result = pacienteService.buscarPorDocumento("99999");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("obtenerEntidadPorDocumento - no existente - lanza ResourceNotFoundException")
    void obtenerEntidadPorDocumento_noExistente_lanzaException() {
        when(pacienteRepository.findByNumeroDocumento("99999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteService.obtenerEntidadPorDocumento("99999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}