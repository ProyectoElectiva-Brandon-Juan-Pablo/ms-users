package com.tiendapc.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de login con token JWT y datos del usuario")
public class LoginResponse {
    
    @Schema(description = "Token JWT para autenticación", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "Tipo de token", example = "Bearer")
    private String type = "Bearer";
    
    @Schema(description = "Datos del usuario autenticado")
    private UsuarioResponse usuario;
    
    // Constructores
    public LoginResponse() {}
    
    public LoginResponse(String token, UsuarioResponse usuario) {
        this.token = token;
        this.usuario = usuario;
    }
    
    // Getters y Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public UsuarioResponse getUsuario() {
        return usuario;
    }
    
    public void setUsuario(UsuarioResponse usuario) {
        this.usuario = usuario;
    }
}