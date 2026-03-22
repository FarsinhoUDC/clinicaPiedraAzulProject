package com.piedrazul.usuarios.dto;

import com.piedrazul.usuarios.domain.Rol;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class LoginResponse {
    private String token;
    private String username;
    private Rol rol;
}
