package com.piedrazul.pacientes.infrastructure.web;

import com.piedrazul.pacientes.application.PacienteService;
import com.piedrazul.pacientes.dto.PacienteRequest;
import com.piedrazul.pacientes.dto.PacienteResponse;
import com.piedrazul.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {
    private final PacienteService pacienteService;

    @GetMapping("/documento/{numero}")
    @PreAuthorize("hasAnyRole('AGENDADOR','ADMIN')")
    public ResponseEntity<ApiResponse<PacienteResponse>> buscarPorDocumento(@PathVariable String numero) {
        Optional<PacienteResponse> p = pacienteService.buscarPorDocumento(numero);
        return p.map(v -> ResponseEntity.ok(ApiResponse.ok(v)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Paciente no encontrado")));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AGENDADOR','ADMIN')")
    public ResponseEntity<ApiResponse<PacienteResponse>> crear(@Valid @RequestBody PacienteRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(pacienteService.crearOActualizar(req)));
    }
}
