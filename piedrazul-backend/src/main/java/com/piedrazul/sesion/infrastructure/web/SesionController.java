package com.piedrazul.sesion.infrastructure.web;

import com.piedrazul.sesion.application.SesionService;
import com.piedrazul.sesion.dto.LoginRequest;
import com.piedrazul.sesion.dto.UsuarioResponse;
import com.piedrazul.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sesion")
@RequiredArgsConstructor
public class SesionController {

    private final SesionService sesionService;

    /**
     * POST /api/sesion/login
     * Inicia sesión para cualquier tipo de usuario (médico o paciente).
     * Retorna el rol para que el frontend redirija a la vista correcta.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UsuarioResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        UsuarioResponse response = sesionService.iniciarSesion(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * GET /api/sesion/usuario/{id}
     * Consulta la información básica de un usuario por su id.
     */
    @GetMapping("/usuario/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtenerUsuario(
            @PathVariable Long id) {
        UsuarioResponse response = sesionService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
