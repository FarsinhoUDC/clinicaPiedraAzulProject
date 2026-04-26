package com.piedrazul.citas.infrastructure.web;

import com.piedrazul.citas.application.AgendamientoService;
import com.piedrazul.citas.application.CitaService;
import com.piedrazul.citas.domain.OrigenCita;
import com.piedrazul.citas.dto.CitaResponse;
import com.piedrazul.citas.dto.CrearCitaRequest;
import com.piedrazul.configuracion.application.FranjaHorariaService;
import com.piedrazul.configuracion.dto.FranjaHorariaResponse;
import com.piedrazul.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;
    private final AgendamientoService agendamientoService;
    private final FranjaHorariaService franjaHorariaService;

    /**
     * HU-01: Listar citas de un medico en una fecha.
     * GET /api/citas?medicoId=1&fecha=2026-03-27
     */
    @GetMapping("/total/{medicoId}/{fecha}")
    public ResponseEntity<ApiResponse<List<CitaResponse>>> listar(
            @PathVariable Long medicoId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<CitaResponse> citas = citaService.listarPorMedicoYFecha(medicoId, fecha);
        return ResponseEntity.ok(ApiResponse.ok("Total: " + citas.size(), citas));
    }

    /**
     * HU-02: Crear cita desde el agendador.
     * POST /api/citas/agendador
     */
    @PostMapping("/agendador")
    public ResponseEntity<ApiResponse<CitaResponse>> crearDesdeAgendador(
            @Valid @RequestBody CrearCitaRequest request) {
        CitaResponse response = agendamientoService.crearCita(request, OrigenCita.AGENDADOR);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Cita creada exitosamente", response));
    }

    /**
     * HU-03: Crear cita desde el paciente (self-service).
     * POST /api/citas/paciente
     */
    @PostMapping("/paciente")
    public ResponseEntity<ApiResponse<CitaResponse>> crearDesdePaciente(
            @Valid @RequestBody CrearCitaRequest request) {
        CitaResponse response = agendamientoService.crearCita(request, OrigenCita.PACIENTE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Cita agendada exitosamente", response));
    }

    /**
     * Franjas disponibles de un medico en una fecha.
     * GET /api/citas/franjas/1?fecha=2026-03-27
     */
    @GetMapping("/franjas/{medicoId}")
    public ResponseEntity<ApiResponse<List<FranjaHorariaResponse>>> obtenerFranjas(
            @PathVariable Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<java.time.LocalTime> ocupadas = citaService.obtenerHorasOcupadas(medicoId, fecha);
        return ResponseEntity.ok(ApiResponse.ok(
                franjaHorariaService.generarFranjas(medicoId, fecha, ocupadas)));
    }

    /**
     * Detalle de una cita.
     * GET /api/citas/1
     */
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<CitaResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(citaService.obtenerPorId(id)));
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<ApiResponse<List<CitaResponse>>> listarPorPaciente(
            @PathVariable Long pacienteId) {
        List<CitaResponse> citas = citaService.listarPorPaciente(pacienteId);
        return ResponseEntity.ok(ApiResponse.ok("Citas del paciente", citas));
    }

    /**
     * Retorna las citas del paciente autenticado, leyendo su número de documento
     * directamente del JWT (preferred_username). Patrón seguro con Keycloak:
     * el paciente no puede ver citas de otros usuarios.
     * GET /api/citas/mis-citas
     */
    @GetMapping("/mis-citas")
    public ResponseEntity<ApiResponse<List<CitaResponse>>> misCitas(
            @AuthenticationPrincipal Jwt jwt) {
        String numeroDocumento = jwt.getClaimAsString("preferred_username");
        List<CitaResponse> citas = citaService.listarPorDocumentoPaciente(numeroDocumento);
        return ResponseEntity.ok(ApiResponse.ok("Mis citas", citas));
    }
}
