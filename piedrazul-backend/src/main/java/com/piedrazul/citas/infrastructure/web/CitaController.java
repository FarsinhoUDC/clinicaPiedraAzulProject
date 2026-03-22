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
import org.springframework.security.access.prepost.PreAuthorize;
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
     * HU-01: Listar citas de un médico en una fecha.
     * GET /api/citas?medicoId=1&fecha=2026-03-27
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('AGENDADOR','ADMIN')")
    public ResponseEntity<ApiResponse<List<CitaResponse>>> listar(
            @RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<CitaResponse> citas = citaService.listarPorMedicoYFecha(medicoId, fecha);
        return ResponseEntity.ok(ApiResponse.ok("Total: " + citas.size(), citas));
    }

    /**
     * HU-02: Crear cita desde el agendador (vía WhatsApp).
     * POST /api/citas/agendador
     */
    @PostMapping("/agendador")
    @PreAuthorize("hasAnyRole('AGENDADOR','ADMIN')")
    public ResponseEntity<ApiResponse<CitaResponse>> crearDesdeAgendador(
            @Valid @RequestBody CrearCitaRequest request) {
        CitaResponse response = agendamientoService.crearCita(request, OrigenCita.AGENDADOR);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Cita creada exitosamente", response));
    }

    /**
     * HU-03: Crear cita desde el paciente (self-service web).
     * POST /api/citas/paciente
     */
    @PostMapping("/paciente")
    @PreAuthorize("hasAnyRole('PACIENTE','ADMIN')")
    public ResponseEntity<ApiResponse<CitaResponse>> crearDesdePaciente(
            @Valid @RequestBody CrearCitaRequest request) {
        CitaResponse response = agendamientoService.crearCita(request, OrigenCita.PACIENTE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Cita agendada exitosamente", response));
    }

    /**
     * Obtener franjas disponibles para un médico en una fecha.
     * Usado por el frontend para mostrar los slots al usuario.
     * GET /api/citas/franjas/1?fecha=2026-03-27
     */
    @GetMapping("/franjas/{medicoId}")
    public ResponseEntity<ApiResponse<List<FranjaHorariaResponse>>> obtenerFranjas(
            @PathVariable Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<java.time.LocalTime> ocupadas = citaService.obtenerHorasOcupadas(medicoId, fecha);
        List<FranjaHorariaResponse> franjas =
                franjaHorariaService.generarFranjas(medicoId, fecha, ocupadas);
        return ResponseEntity.ok(ApiResponse.ok(franjas));
    }

    /**
     * Obtener detalle de una cita por ID.
     * GET /api/citas/1
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENDADOR','ADMIN','PACIENTE')")
    public ResponseEntity<ApiResponse<CitaResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(citaService.obtenerPorId(id)));
    }
}
