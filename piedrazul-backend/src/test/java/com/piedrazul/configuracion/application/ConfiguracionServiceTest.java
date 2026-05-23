package com.piedrazul.configuracion.application;

import com.piedrazul.configuracion.domain.ConfiguracionSistema;
import com.piedrazul.configuracion.domain.DisponibilidadMedico;
import com.piedrazul.configuracion.dto.ConfiguracionSistemaRequest;
import com.piedrazul.configuracion.dto.DisponibilidadMedicoRequest;
import com.piedrazul.configuracion.dto.DisponibilidadMedicoResponse;
import com.piedrazul.configuracion.infrastructure.persistence.ConfiguracionSistemaRepository;
import com.piedrazul.configuracion.infrastructure.persistence.DisponibilidadMedicoRepository;
import com.piedrazul.medicos.application.MedicoService;
import com.piedrazul.medicos.domain.Medico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfiguracionService - pruebas unitarias")
class ConfiguracionServiceTest {

    @Mock private ConfiguracionSistemaRepository configuracionSistemaRepository;
    @Mock private DisponibilidadMedicoRepository disponibilidadMedicoRepository;
    @Mock private MedicoService medicoService;
    @InjectMocks private ConfiguracionService configuracionService;

    private Medico medico;
    private DisponibilidadMedico disponibilidad;

    @BeforeEach
    void setUp() {
        medico = Medico.nuevo("Carlos", "Gomez", "carlos@test.com", "pass123",
                "1234", "Medicina General", null, null, null);
        medico.setId(1L);

        disponibilidad = DisponibilidadMedico.builder()
                .id(1L).medico(medico)
                .diasSemana(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(12, 0))
                .intervaloMinutos(30)
                .build();
    }

    @Test
    @DisplayName("guardarConfiguracionSistema - nueva config - guarda exitosa")
    void guardarConfiguracionSistema_nuevaGuarda() {
        ConfiguracionSistemaRequest request = new ConfiguracionSistemaRequest();
        request.setVentanaSemanas(8);

        when(configuracionSistemaRepository.findAll()).thenReturn(List.of());
        when(configuracionSistemaRepository.save(any(ConfiguracionSistema.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConfiguracionSistema resultado = configuracionService.guardarConfiguracionSistema(request);

        assertThat(resultado.getVentanaSemanas()).isEqualTo(8);
        verify(configuracionSistemaRepository).save(any(ConfiguracionSistema.class));
    }

    @Test
    @DisplayName("obtenerConfiguracionSistema - sin config - retorna默认值 (4 semanas)")
    void obtenerConfiguracionSistema_vacia_retornaDefault() {
        when(configuracionSistemaRepository.findAll()).thenReturn(List.of());

        ConfiguracionSistema resultado = configuracionService.obtenerConfiguracionSistema();

        assertThat(resultado.getVentanaSemanas()).isEqualTo(4);
    }

    @Test
    @DisplayName("guardarDisponibilidad - nuevo - guarda exitosamente")
    void guardarDisponibilidad_nuevaGuarda() {
        DisponibilidadMedicoRequest request = new DisponibilidadMedicoRequest();
        request.setMedicoId(1L);
        request.setDiasSemana(Set.of(DayOfWeek.MONDAY));
        request.setHoraInicio(LocalTime.of(9, 0));
        request.setHoraFin(LocalTime.of(17, 0));
        request.setIntervaloMinutos(60);

        when(medicoService.obtenerPorId(1L)).thenReturn(medico);
        when(disponibilidadMedicoRepository.findByMedicoId(1L)).thenReturn(Optional.empty());
        when(disponibilidadMedicoRepository.save(any(DisponibilidadMedico.class)))
                .thenAnswer(invocation -> {
                    DisponibilidadMedico d = invocation.getArgument(0);
                    d.setId(1L);
                    return d;
                });

        DisponibilidadMedicoResponse response = configuracionService.guardarDisponibilidad(request);

        assertThat(response).isNotNull();
        verify(disponibilidadMedicoRepository).save(any(DisponibilidadMedico.class));
    }

    @Test
    @DisplayName("guardarDisponibilidad - existente - actualiza")
    void guardarDisponibilidad_existente_actualiza() {
        DisponibilidadMedicoRequest request = new DisponibilidadMedicoRequest();
        request.setMedicoId(1L);
        request.setDiasSemana(Set.of(DayOfWeek.TUESDAY));
        request.setHoraInicio(LocalTime.of(10, 0));
        request.setHoraFin(LocalTime.of(16, 0));
        request.setIntervaloMinutos(30);

        when(medicoService.obtenerPorId(1L)).thenReturn(medico);
        when(disponibilidadMedicoRepository.findByMedicoId(1L)).thenReturn(Optional.of(disponibilidad));
        when(disponibilidadMedicoRepository.save(any(DisponibilidadMedico.class)))
                .thenReturn(disponibilidad);

        DisponibilidadMedicoResponse response = configuracionService.guardarDisponibilidad(request);

        assertThat(response.getDiasSemana()).contains(DayOfWeek.TUESDAY);
    }

    @Test
    @DisplayName("listarDisponibilidades - retorna lista")
    void listarDisponibilidades_retornaLista() {
        when(disponibilidadMedicoRepository.findAll()).thenReturn(List.of(disponibilidad));

        List<DisponibilidadMedicoResponse> resultado = configuracionService.listarDisponibilidades();

        assertThat(resultado).hasSize(1);
    }
}