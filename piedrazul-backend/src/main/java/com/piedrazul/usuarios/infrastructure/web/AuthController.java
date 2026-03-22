package com.piedrazul.usuarios.infrastructure.web;

import com.piedrazul.shared.response.ApiResponse;
import com.piedrazul.usuarios.application.AuthService;
import com.piedrazul.usuarios.dto.LoginRequest;
import com.piedrazul.usuarios.dto.LoginResponse;
import com.piedrazul.usuarios.dto.RegistroUsuarioRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<LoginResponse>> registro(@Valid @RequestBody RegistroUsuarioRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario registrado exitosamente", authService.registrarYLogin(request)));
    }
}
