package com.piedrazul.agendadores.infrastructure.web;

import com.piedrazul.agendadores.application.AgendadorService;
import com.piedrazul.agendadores.dto.AgendadorRequest;
import com.piedrazul.agendadores.dto.AgendadorResponse;
import com.piedrazul.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agendadores")
@RequiredArgsConstructor
public class AgendadorController {

    private final AgendadorService agendadorService;

    @PostMapping
    public ResponseEntity<ApiResponse<AgendadorResponse>> crear(
            @Valid @RequestBody AgendadorRequest req) {
        AgendadorResponse agendador = agendadorService.crear(req);
        return ResponseEntity.ok(ApiResponse.ok("Agendador registrado exitosamente", agendador));
    }
}
