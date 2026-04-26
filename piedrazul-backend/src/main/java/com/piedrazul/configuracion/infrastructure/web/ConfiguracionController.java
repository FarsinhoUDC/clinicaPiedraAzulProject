package com.piedrazul.configuracion.infrastructure.web;

import com.piedrazul.configuracion.application.ConfiguracionService;
import com.piedrazul.configuracion.dto.ConfiguracionSistemaRequest;
import com.piedrazul.configuracion.dto.DisponibilidadMedicoRequest;
import com.piedrazul.configuracion.dto.DisponibilidadMedicoResponse;
import com.piedrazul.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Doble protección en endpoints de escritura:
 *  1. SecurityFilterChain: POST /api/configuracion/** → hasRole("ADMIN")
 *  2. @PreAuthorize en cada método POST (defensa en profundidad)
 *
 * El GET /api/configuracion es accesible por PACIENTE, AGENDADOR y MEDICO
 * (controlado únicamente por el SecurityFilterChain).
 */
@RestController
@RequestMapping("/api/configuracion")
@RequiredArgsConstructor
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    /** Lectura de disponibilidades — accesible por todos los roles autenticados con permiso */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DisponibilidadMedicoResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(configuracionService.listarDisponibilidades()));
    }

    /** Escritura — exclusivo ADMIN */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/disponibilidad")
    public ResponseEntity<ApiResponse<DisponibilidadMedicoResponse>> guardarDisponibilidad(
            @Valid @RequestBody DisponibilidadMedicoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(configuracionService.guardarDisponibilidad(request)));
    }

    /** Escritura — exclusivo ADMIN */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sistema")
    public ResponseEntity<ApiResponse<Void>> guardarConfigSistema(
            @Valid @RequestBody ConfiguracionSistemaRequest request) {
        configuracionService.guardarConfiguracionSistema(request);
        return ResponseEntity.ok(ApiResponse.ok("Configuracion guardada", null));
    }
}
