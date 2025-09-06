package com.tiendapc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos requeridos para iniciar sesión")
public class LoginRequest {
    
    @NotBlank(message = "Username o email es obligatorio")
    @Schema(description = "Username o email del usuario", example = "admin@techstore.com")
    private String identifier;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(description = "Contraseña del usuario", example = "admin123")
    private String password;
    
    // Constructores
    public LoginRequest() {}
    
    public LoginRequest(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }
    
    // Getters y Setters
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}