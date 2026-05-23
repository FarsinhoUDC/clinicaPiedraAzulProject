package com.piedrazul.citas.application;

import com.piedrazul.citas.domain.Cita;
import com.piedrazul.citas.domain.OrigenCita;
import com.piedrazul.citas.dto.CitaResponse;
import com.piedrazul.citas.dto.CrearCitaRequest;
import com.piedrazul.configuracion.application.FranjaHorariaService;
import com.piedrazul.configuracion.domain.DisponibilidadMedico;
import com.piedrazul.medicos.application.MedicoService;
import com.piedrazul.medicos.domain.Medico;
import com.piedrazul.pacientes.application.PacienteService;
import com.piedrazul.pacientes.domain.Genero;
import com.piedrazul.pacientes.domain.Paciente;
import com.piedrazul.pacientes.dto.PacienteRequest;
import com.piedrazul.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgendamientoService - pruebas unitarias")
class AgendamientoServiceTest {

    @Mock private PacienteService pacienteService;
    @Mock private MedicoService medicoService;
    @Mock private FranjaHorariaService franjaHorariaService;
    @Mock private CitaService citaService;
    @InjectMocks private AgendamientoService agendamientoService;

    private Medico medico;
    private Paciente paciente;
    private DisponibilidadMedico disponibilidad;
    private Cita cita;

    @BeforeEach
    void setUp() {
        medico = Medico.nuevo("Carlos", "Gomez", "carlos@test.com", "pass123",
                "1234", "Medicina General", null, null, null);
        medico.setId(1L);

        paciente = Paciente.nuevo("Maria", "Lopez", "maria@test.com", "pass456",
                "12345", "3001234567", Genero.MUJER, null);
        paciente.setId(2L);

        disponibilidad = DisponibilidadMedico.builder()
                .id(1L).medico(medico)
                .diasSemana(Set.of(java.time.DayOfWeek.MONDAY))
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(12, 0))
                .intervaloMinutos(30)
                .build();

        cita = Cita.builder()
                .id(1L).medico(medico).paciente(paciente)
                .fechaHora(LocalDateTime.of(2026, 3, 23, 9, 0))
                .origen(OrigenCita.PACIENTE)
                .build();
    }

    @Test
    @DisplayName("crearCita - datos validos - crea exitosamente")
    void crearCita_datosValidos_crea() {
        CrearCitaRequest request = new CrearCitaRequest();
        request.setMedicoId(1L);
        request.setFechaHora(LocalDateTime.of(2026, 3, 23, 9, 0));

        PacienteRequest pacienteReq = new PacienteRequest();
        pacienteReq.setNumeroDocumento("12345");
        pacienteReq.setNombres("Maria");
        pacienteReq.setApellidos("Lopez");
        request.setPaciente(pacienteReq);

        when(pacienteService.obtenerEntidadPorDocumento("12345")).thenReturn(paciente);
        when(medicoService.obtenerPorId(1L)).thenReturn(medico);
        when(franjaHorariaService.obtenerDisponibilidad(1L)).thenReturn(disponibilidad);
        when(citaService.guardar(any(Cita.class))).thenReturn(cita);
        when(citaService.toResponse(cita)).thenReturn(CitaResponse.builder()
                .id(1L)
                .medicoId(1L)
                .pacienteId(2L)
                .fechaHora(cita.getFechaHora())
                .build());

        CitaResponse response = agendamientoService.crearCita(request, OrigenCita.PACIENTE);

        assertThat(response).isNotNull();
        verify(pacienteService).crearOActualizar(pacienteReq);
        verify(citaService).guardar(any(Cita.class));
    }

    @Test
    @DisplayName("crearCita - dia no habil - lanza BusinessException")
    void crearCita_diaNoHabil_lanzaException() {
        CrearCitaRequest request = new CrearCitaRequest();
        request.setMedicoId(1L);
        request.setFechaHora(LocalDateTime.of(2026, 3, 24, 9, 0)); //-Martes

        PacienteRequest pacienteReq = new PacienteRequest();
        pacienteReq.setNumeroDocumento("12345");
        request.setPaciente(pacienteReq);

        when(pacienteService.obtenerEntidadPorDocumento("12345")).thenReturn(paciente);
        when(medicoService.obtenerPorId(1L)).thenReturn(medico);
        when(franjaHorariaService.obtenerDisponibilidad(1L)).thenReturn(disponibilidad);

        assertThatThrownBy(() -> agendamientoService.crearCita(request, OrigenCita.PACIENTE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no atiende");
    }

    @Test
    @DisplayName("crearCita - hora fuera de rango - lanza BusinessException")
    void crearCita_horaFueraRango_lanzaException() {
        CrearCitaRequest request = new CrearCitaRequest();
        request.setMedicoId(1L);
        request.setFechaHora(LocalDateTime.of(2026, 3, 23, 14, 0)); // 14:00 fuera del rango

        PacienteRequest pacienteReq = new PacienteRequest();
        pacienteReq.setNumeroDocumento("12345");
        request.setPaciente(pacienteReq);

        when(pacienteService.obtenerEntidadPorDocumento("12345")).thenReturn(paciente);
        when(medicoService.obtenerPorId(1L)).thenReturn(medico);
        when(franjaHorariaService.obtenerDisponibilidad(1L)).thenReturn(disponibilidad);

        assertThatThrownBy(() -> agendamientoService.crearCita(request, OrigenCita.PACIENTE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("fuera de la franja");
    }

    @Test
    @DisplayName("crearCita - slot no valido - lanza BusinessException")
    void crearCita_slotNoValido_lanzaException() {
        CrearCitaRequest request = new CrearCitaRequest();
        request.setMedicoId(1L);
        request.setFechaHora(LocalDateTime.of(2026, 3, 23, 9, 15)); // 09:15 no es multiplo de 30

        PacienteRequest pacienteReq = new PacienteRequest();
        pacienteReq.setNumeroDocumento("12345");
        request.setPaciente(pacienteReq);

        when(pacienteService.obtenerEntidadPorDocumento("12345")).thenReturn(paciente);
        when(medicoService.obtenerPorId(1L)).thenReturn(medico);
        when(franjaHorariaService.obtenerDisponibilidad(1L)).thenReturn(disponibilidad);

        assertThatThrownBy(() -> agendamientoService.crearCita(request, OrigenCita.PACIENTE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("slot");
    }
}