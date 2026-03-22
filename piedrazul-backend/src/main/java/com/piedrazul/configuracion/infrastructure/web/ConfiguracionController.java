package com.piedrazul.configuracion.infrastructure.web;

import com.piedrazul.configuracion.application.ConfiguracionService;
import com.piedrazul.configuracion.application.FranjaHorariaService;
import com.piedrazul.configuracion.dto.*;
import com.piedrazul.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/configuracion")
@RequiredArgsConstructor
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;
    private final FranjaHorariaService franjaHorariaService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DisponibilidadMedicoResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(configuracionService.listarDisponibilidades()));
    }

    @PostMapping("/disponibilidad")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DisponibilidadMedicoResponse>> guardarDisponibilidad(
            @Valid @RequestBody DisponibilidadMedicoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(configuracionService.guardarDisponibilidad(request)));
    }

    @PostMapping("/sistema")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> guardarConfigSistema(
            @Valid @RequestBody ConfiguracionSistemaRequest request) {
        configuracionService.guardarConfiguracionSistema(request);
        return ResponseEntity.ok(ApiResponse.ok("Configuracion guardada", null));
    }

    // Endpoint publico para que el frontend obtenga las franjas disponibles
    @GetMapping("/franjas/{medicoId}")
    public ResponseEntity<ApiResponse<List<FranjaHorariaResponse>>> obtenerFranjas(
            @PathVariable Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<java.time.LocalTime> horasOcupadas = List.of(); // se llena desde CitaService
        return ResponseEntity.ok(ApiResponse.ok(
                franjaHorariaService.generarFranjas(medicoId, fecha, horasOcupadas)));
    }
}
