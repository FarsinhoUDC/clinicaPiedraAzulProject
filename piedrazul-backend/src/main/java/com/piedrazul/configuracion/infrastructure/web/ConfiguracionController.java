package com.piedrazul.configuracion.infrastructure.web;

import com.piedrazul.configuracion.application.ConfiguracionService;
import com.piedrazul.configuracion.dto.ConfiguracionSistemaRequest;
import com.piedrazul.configuracion.dto.DisponibilidadMedicoRequest;
import com.piedrazul.configuracion.dto.DisponibilidadMedicoResponse;
import com.piedrazul.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configuracion")
@RequiredArgsConstructor
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DisponibilidadMedicoResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(configuracionService.listarDisponibilidades()));
    }

    @PostMapping("/disponibilidad")
    public ResponseEntity<ApiResponse<DisponibilidadMedicoResponse>> guardarDisponibilidad(
            @Valid @RequestBody DisponibilidadMedicoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(configuracionService.guardarDisponibilidad(request)));
    }

    @PostMapping("/sistema")
    public ResponseEntity<ApiResponse<Void>> guardarConfigSistema(
            @Valid @RequestBody ConfiguracionSistemaRequest request) {
        configuracionService.guardarConfiguracionSistema(request);
        return ResponseEntity.ok(ApiResponse.ok("Configuracion guardada", null));
    }
}
