package com.piedrazul.configuracion.application;

import com.piedrazul.configuracion.domain.DisponibilidadMedico;
import com.piedrazul.configuracion.dto.FranjaHorariaResponse;
import com.piedrazul.configuracion.infrastructure.persistence.DisponibilidadMedicoRepository;
import com.piedrazul.medicos.domain.Medico;
import com.piedrazul.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FranjaHorariaServiceTest {

    @Mock private DisponibilidadMedicoRepository disponibilidadMedicoRepository;
    @InjectMocks private FranjaHorariaService franjaHorariaService;

    private DisponibilidadMedico disponibilidad;
    private Medico medico;

    @BeforeEach
    void setUp() {
        medico = Medico.builder().id(1L).nombres("Dr. Carlos").apellidos("Gomez").build();
        disponibilidad = DisponibilidadMedico.builder()
                .id(1L).medico(medico)
                .diasSemana(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(12, 0))
                .intervaloMinutos(30)
                .build();
    }

    @Test
    void generarFranjas_diaHabil_retornaFranjasCorrectas() {
        when(disponibilidadMedicoRepository.findByMedicoId(1L)).thenReturn(Optional.of(disponibilidad));

        // Buscar un lunes
        LocalDate lunes = LocalDate.of(2026, 3, 23);
        List<FranjaHorariaResponse> franjas = franjaHorariaService.generarFranjas(1L, lunes, List.of());

        // 08:00 a 11:30 con intervalo 30 min = 8 franjas
        assertThat(franjas).hasSize(8);
        assertThat(franjas.get(0).getHora()).isEqualTo(LocalTime.of(8, 0));
        assertThat(franjas.get(0).isDisponible()).isTrue();
    }

    @Test
    void generarFranjas_diaNoHabil_retornaListaVacia() {
        when(disponibilidadMedicoRepository.findByMedicoId(1L)).thenReturn(Optional.of(disponibilidad));

        // Martes no esta en diasSemana
        LocalDate martes = LocalDate.of(2026, 3, 24);
        List<FranjaHorariaResponse> franjas = franjaHorariaService.generarFranjas(1L, martes, List.of());

        assertThat(franjas).isEmpty();
    }

    @Test
    void generarFranjas_horaOcupada_marcaComoNoDisponible() {
        when(disponibilidadMedicoRepository.findByMedicoId(1L)).thenReturn(Optional.of(disponibilidad));

        LocalDate lunes = LocalDate.of(2026, 3, 23);
        List<FranjaHorariaResponse> franjas = franjaHorariaService.generarFranjas(
                1L, lunes, List.of(LocalTime.of(8, 0)));

        assertThat(franjas.get(0).isDisponible()).isFalse();
        assertThat(franjas.get(1).isDisponible()).isTrue();
    }

    @Test
    void generarFranjas_medicoSinConfiguracion_lanzaResourceNotFoundException() {
        when(disponibilidadMedicoRepository.findByMedicoId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> franjaHorariaService.generarFranjas(99L, LocalDate.now(), List.of()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
