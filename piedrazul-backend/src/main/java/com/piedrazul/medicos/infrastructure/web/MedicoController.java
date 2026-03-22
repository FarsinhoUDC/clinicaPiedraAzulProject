package com.piedrazul.medicos.infrastructure.web;

import com.piedrazul.medicos.application.MedicoService;
import com.piedrazul.medicos.dto.MedicoRequest;
import com.piedrazul.medicos.dto.MedicoResponse;
import com.piedrazul.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/medicos")
@RequiredArgsConstructor
public class MedicoController {
    private final MedicoService medicoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicoResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(medicoService.listarActivos()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MedicoResponse>> crear(@Valid @RequestBody MedicoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(medicoService.crear(request)));
    }
}
