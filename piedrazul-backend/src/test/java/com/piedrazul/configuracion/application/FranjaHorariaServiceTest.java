package com.piedrazul.configuracion.application;

import com.piedrazul.configuracion.domain.DisponibilidadMedico;
import com.piedrazul.configuracion.dto.FranjaHorariaResponse;
import com.piedrazul.configuracion.infrastructure.persistence.DisponibilidadMedicoRepository;
import com.piedrazul.medicos.domain.Medico;
import com.piedrazul.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("FranjaHorariaService - pruebas unitarias")
class FranjaHorariaServiceTest {

    @Mock private DisponibilidadMedicoRepository disponibilidadMedicoRepository;
    @InjectMocks private FranjaHorariaService franjaHorariaService;

    private DisponibilidadMedico disponibilidad;

    @BeforeEach
    void setUp() {
        // Medico construido con factory method — incluye campos heredados de Usuario
        Medico medico = Medico.nuevo("Carlos", "Gomez",
                "carlos.gomez@test.com", "pass123", "1234", "Medicina General");
        medico.setId(1L);

        disponibilidad = DisponibilidadMedico.builder()
                .id(1L).medico(medico)
                .diasSemana(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(12, 0))
                .intervaloMinutos(30)
                .build();
    }

    @Test
    @DisplayName("generarFranjas - dia habil - retorna 8 franjas entre 08:00 y 11:30")
    void generarFranjas_diaHabil_retornaFranjasCorrectas() {
        when(disponibilidadMedicoRepository.findByMedicoId(1L))
                .thenReturn(Optional.of(disponibilidad));

        // Lunes 23 de marzo 2026
        LocalDate lunes = LocalDate.of(2026, 3, 23);
        List<FranjaHorariaResponse> franjas =
                franjaHorariaService.generarFranjas(1L, lunes, List.of());

        // 08:00, 08:30, 09:00, 09:30, 10:00, 10:30, 11:00, 11:30 = 8 franjas
        assertThat(franjas).hasSize(8);
        assertThat(franjas.get(0).getHora()).isEqualTo(LocalTime.of(8, 0));
        assertThat(franjas.get(0).isDisponible()).isTrue();
    }

    @Test
    @DisplayName("generarFranjas - dia no habil - retorna lista vacia")
    void generarFranjas_diaNoHabil_retornaListaVacia() {
        when(disponibilidadMedicoRepository.findByMedicoId(1L))
                .thenReturn(Optional.of(disponibilidad));

        // Martes 24 de marzo 2026 (no esta en diasSemana)
        LocalDate martes = LocalDate.of(2026, 3, 24);
        List<FranjaHorariaResponse> franjas =
                franjaHorariaService.generarFranjas(1L, martes, List.of());

        assertThat(franjas).isEmpty();
    }

    @Test
    @DisplayName("generarFranjas - hora ocupada - marca slot como no disponible")
    void generarFranjas_horaOcupada_marcaComoNoDisponible() {
        when(disponibilidadMedicoRepository.findByMedicoId(1L))
                .thenReturn(Optional.of(disponibilidad));

        LocalDate lunes = LocalDate.of(2026, 3, 23);
        List<FranjaHorariaResponse> franjas = franjaHorariaService.generarFranjas(
                1L, lunes, List.of(LocalTime.of(8, 0)));

        assertThat(franjas.get(0).isDisponible()).isFalse();
        assertThat(franjas.get(1).isDisponible()).isTrue();
    }

    @Test
    @DisplayName("generarFranjas - medico sin configuracion - lanza ResourceNotFoundException")
    void generarFranjas_sinConfiguracion_lanzaException() {
        when(disponibilidadMedicoRepository.findByMedicoId(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                franjaHorariaService.generarFranjas(99L, LocalDate.now(), List.of()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
