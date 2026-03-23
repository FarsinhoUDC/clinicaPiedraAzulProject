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

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PacienteService - pruebas unitarias")
class PacienteServiceTest {

    @Mock private PacienteRepository pacienteRepository;
    @InjectMocks private PacienteService pacienteService;

    private Paciente paciente;
    private PacienteRequest request;

    @BeforeEach
    void setUp() {
        paciente = Paciente.builder().id(1L).numeroDocumento("12345")
                .nombres("Maria").apellidos("Lopez")
                .celular("3001234567").genero(Genero.MUJER).build();
        request = new PacienteRequest();
        request.setNumeroDocumento("12345");
        request.setNombres("Maria");
        request.setApellidos("Lopez");
        request.setCelular("3001234567");
        request.setGenero(Genero.MUJER);
    }

    @Test
    @DisplayName("crearOActualizar - paciente nuevo - guarda y retorna response")
    void crearOActualizar_pacienteNuevo_guardaYRetornaResponse() {
        when(pacienteRepository.findByNumeroDocumento("12345")).thenReturn(Optional.empty());
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);

        PacienteResponse resultado = pacienteService.crearOActualizar(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNumeroDocumento()).isEqualTo("12345");
        verify(pacienteRepository).save(any(Paciente.class));
    }

    @Test
    @DisplayName("crearOActualizar - paciente existente - actualiza datos")
    void crearOActualizar_pacienteExistente_actualizaDatos() {
        when(pacienteRepository.findByNumeroDocumento("12345")).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);

        PacienteResponse resultado = pacienteService.crearOActualizar(request);

        assertThat(resultado.getNombres()).isEqualTo("Maria");
        verify(pacienteRepository).save(any(Paciente.class));
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
