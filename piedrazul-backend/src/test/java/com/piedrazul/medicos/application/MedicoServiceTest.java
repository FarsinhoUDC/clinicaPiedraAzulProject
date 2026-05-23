package com.piedrazul.medicos.application;

import com.piedrazul.medicos.domain.Medico;
import com.piedrazul.medicos.dto.MedicoRequest;
import com.piedrazul.medicos.dto.MedicoResponse;
import com.piedrazul.medicos.infrastructure.persistence.MedicoRepository;
import com.piedrazul.pacientes.infrastructure.KeycloakService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicoService - pruebas unitarias")
class MedicoServiceTest {

    @Mock private MedicoRepository medicoRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private KeycloakService keycloakService;
    @InjectMocks private MedicoService medicoService;

    private Medico medico;

    @BeforeEach
    void setUp() {
        medico = Medico.nuevo("Carlos", "Gomez", "carlos@test.com", "hashedpass",
                "1234", "Medicina General", null, null, null);
        medico.setId(1L);
    }

    @Test
    @DisplayName("listarActivos - retorna lista de medicos activos")
    void listarActivos_retornaLista() {
        when(medicoRepository.findByActivoTrue()).thenReturn(List.of(medico));

        List<MedicoResponse> resultado = medicoService.listarActivos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombres()).isEqualTo("Carlos");
        verify(medicoRepository).findByActivoTrue();
    }

    @Test
    @DisplayName("obtenerPorId - id existente - retorna medico")
    void obtenerPorId_existe_retornaMedico() {
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));

        Medico resultado = medicoService.obtenerPorId(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombres()).isEqualTo("Carlos");
    }

    @Test
    @DisplayName("obtenerPorId - id no existente - lanza ResourceNotFoundException")
    void obtenerPorId_noExiste_lanzaException() {
        when(medicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicoService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("crear - nuevo medico - guarda y retorna response")
    void crear_nuevoMedico_guardaYRetorna() {
        MedicoRequest request = new MedicoRequest();
        request.setNombres("Pedro");
        request.setApellidos("Ramirez");
        request.setCorreo("pedro@test.com");
        request.setNumeroDocumento("5678");
        request.setEspecialidad("Cardiología");
        request.setCelular("3001234567");
        request.setGenero("HOMBRE");
        request.setFechaNacimiento(LocalDate.of(1985, 5, 15));

        when(medicoRepository.existsByNumeroDocumento("5678")).thenReturn(false);
        when(passwordEncoder.encode("5678")).thenReturn("encoded_password");
        when(medicoRepository.save(any(Medico.class))).thenReturn(medico);

        MedicoResponse response = medicoService.crear(request);

        assertThat(response).isNotNull();
        verify(medicoRepository).save(any(Medico.class));
        verify(keycloakService).crearMedico(request);
    }

    @Test
    @DisplayName("crear - documento duplicado - lanza BusinessException")
    void crear_documentoDuplicado_lanzaException() {
        MedicoRequest request = new MedicoRequest();
        request.setNombres("Pedro");
        request.setApellidos("Ramirez");
        request.setCorreo("pedro@test.com");
        request.setNumeroDocumento("1234");
        request.setEspecialidad("Cardiología");

        when(medicoRepository.existsByNumeroDocumento("1234")).thenReturn(true);

        assertThatThrownBy(() -> medicoService.crear(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ya existe un usuario registrado");
    }

    @Test
    @DisplayName("crear - genero nulo - usa valores por defecto")
    void crear_generoNulo_sinError() {
        MedicoRequest request = new MedicoRequest();
        request.setNombres("Pedro");
        request.setApellidos("Ramirez");
        request.setCorreo("pedro@test.com");
        request.setNumeroDocumento("9999");
        request.setEspecialidad("Cardiología");
        request.setGenero(null);
        request.setFechaNacimiento(null);

        when(medicoRepository.existsByNumeroDocumento("9999")).thenReturn(false);
        when(passwordEncoder.encode("9999")).thenReturn("encoded_password");
        when(medicoRepository.save(any(Medico.class))).thenReturn(medico);

        MedicoResponse response = medicoService.crear(request);

        assertThat(response).isNotNull();
    }
}