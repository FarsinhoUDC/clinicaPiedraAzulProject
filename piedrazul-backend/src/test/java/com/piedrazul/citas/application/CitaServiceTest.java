package com.piedrazul.citas.application;

import com.piedrazul.citas.domain.Cita;
import com.piedrazul.citas.domain.OrigenCita;
import com.piedrazul.citas.dto.CitaResponse;
import com.piedrazul.citas.infrastructure.persistence.CitaRepository;
import com.piedrazul.medicos.domain.Medico;
import com.piedrazul.pacientes.domain.Genero;
import com.piedrazul.pacientes.domain.Paciente;
import com.piedrazul.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CitaService - pruebas unitarias")
class CitaServiceTest {

    @Mock private CitaRepository citaRepository;
    @InjectMocks private CitaService citaService;

    private Medico medico;
    private Paciente paciente;
    private Cita cita;
    private LocalDateTime fechaHora;

    @BeforeEach
    void setUp() {
        // Medico construido con factory method — incluye campos heredados de Usuario
        medico = Medico.nuevo("Juan", "Perez",
                "juan.perez@test.com", "pass123", "General");
        medico.setId(1L);

        // Paciente construido con factory method — incluye campos heredados de Usuario
        paciente = Paciente.nuevo("Maria", "Lopez",
                "maria.lopez@test.com", "pass123",
                "12345", "3001234567", Genero.MUJER, null);
        paciente.setId(1L);

        fechaHora = LocalDateTime.now().plusDays(1)
                .withHour(9).withMinute(0).withSecond(0).withNano(0);

        cita = Cita.builder()
                .id(1L).medico(medico).paciente(paciente)
                .fechaHora(fechaHora).origen(OrigenCita.AGENDADOR)
                .build();
    }

    @Test
    @DisplayName("guardar - cita valida - retorna cita guardada")
    void guardar_citaValida_retornaCitaGuardada() {
        when(citaRepository.existsByMedicoIdAndFechaHora(anyLong(), any())).thenReturn(false);
        when(citaRepository.save(any(Cita.class))).thenReturn(cita);

        Cita resultado = citaService.guardar(cita);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        verify(citaRepository).save(cita);
    }

    @Test
    @DisplayName("guardar - horario ocupado - lanza BusinessException")
    void guardar_horarioOcupado_lanzaBusinessException() {
        when(citaRepository.existsByMedicoIdAndFechaHora(anyLong(), any())).thenReturn(true);

        assertThatThrownBy(() -> citaService.guardar(cita))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ya existe una cita");
    }

    @Test
    @DisplayName("listarPorMedicoYFecha - retorna lista mapeada correctamente")
    void listarPorMedicoYFecha_retornaListaMapeada() {
        when(citaRepository.findByMedicoIdAndFecha(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(cita));

        List<CitaResponse> resultado = citaService.listarPorMedicoYFecha(1L, fechaHora.toLocalDate());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombrePaciente()).isEqualTo("Maria Lopez");
        assertThat(resultado.get(0).getNombreMedico()).isEqualTo("Juan Perez");
    }

    @Test
    @DisplayName("listarPorMedicoYFecha - sin citas - retorna lista vacia")
    void listarPorMedicoYFecha_sinCitas_retornaListaVacia() {
        when(citaRepository.findByMedicoIdAndFecha(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<CitaResponse> resultado = citaService.listarPorMedicoYFecha(1L, LocalDate.now());

        assertThat(resultado).isEmpty();
    }
}
