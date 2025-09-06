package com.tiendapc.model;

public enum Role {
    ADMIN("Administrador"),
    CUSTOMER("Comprador");
    
    private final String displayName;
    
    Role(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}