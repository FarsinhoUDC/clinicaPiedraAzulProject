package com.piedrazul.pacientes.application;

import com.piedrazul.pacientes.domain.Genero;
import com.piedrazul.pacientes.domain.Paciente;
import com.piedrazul.pacientes.dto.PacienteRequest;
import com.piedrazul.pacientes.dto.PacienteResponse;
import com.piedrazul.pacientes.infrastructure.persistence.PacienteRepository;
import com.piedrazul.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
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
class PacienteServiceTest {

    @Mock private PacienteRepository pacienteRepository;
    @InjectMocks private PacienteService pacienteService;

    private Paciente paciente;
    private PacienteRequest request;

    @BeforeEach
    void setUp() {
        paciente = Paciente.builder().id(1L).numeroDocumento("12345")
                .nombres("Maria").apellidos("Lopez").celular("3001234567").genero(Genero.MUJER).build();
        request = new PacienteRequest();
        request.setNumeroDocumento("12345");
        request.setNombres("Maria");
        request.setApellidos("Lopez");
        request.setCelular("3001234567");
        request.setGenero(Genero.MUJER);
    }

    @Test
    void crearOActualizar_pacienteNuevo_guardaYRetornaResponse() {
        when(pacienteRepository.findByNumeroDocumento("12345")).thenReturn(Optional.empty());
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);

        PacienteResponse resultado = pacienteService.crearOActualizar(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNumeroDocumento()).isEqualTo("12345");
        verify(pacienteRepository).save(any(Paciente.class));
    }

    @Test
    void buscarPorDocumento_existente_retornaOptionalConValor() {
        when(pacienteRepository.findByNumeroDocumento("12345")).thenReturn(Optional.of(paciente));

        Optional<PacienteResponse> resultado = pacienteService.buscarPorDocumento("12345");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombres()).isEqualTo("Maria");
    }

    @Test
    void buscarPorDocumento_noExistente_retornaOptionalVacio() {
        when(pacienteRepository.findByNumeroDocumento("99999")).thenReturn(Optional.empty());

        Optional<PacienteResponse> resultado = pacienteService.buscarPorDocumento("99999");

        assertThat(resultado).isEmpty();
    }

    @Test
    void obtenerEntidadPorDocumento_noExistente_lanzaResourceNotFoundException() {
        when(pacienteRepository.findByNumeroDocumento("99999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteService.obtenerEntidadPorDocumento("99999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
