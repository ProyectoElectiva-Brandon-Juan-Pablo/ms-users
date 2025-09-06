package com.tiendapc.controller;

import com.tiendapc.config.JwtUtil;
import com.tiendapc.dto.*;
import com.tiendapc.model.Role;
import com.tiendapc.model.Usuario;
import com.tiendapc.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Gestión de Usuarios", description = "API para la gestión de usuarios en TechStore - Sistema de microservicios")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo usuario", 
               description = "Permite registrar un nuevo usuario en el sistema TechStore. " +
                           "Por defecto se asigna el rol CUSTOMER, pero se puede especificar ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", 
                    description = "Usuario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
        @ApiResponse(responseCode = "400", 
                    description = "Datos inválidos o usuario ya existe",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<?> registrarUsuario(
            @Valid @RequestBody 
            @Parameter(description = "Datos del usuario a registrar") 
            RegistroUsuarioRequest request) {
        try {
            UsuarioResponse usuarioResponse = usuarioService.registrarUsuario(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Usuario registrado exitosamente en TechStore");
            response.put("usuario", usuarioResponse);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", 
               description = "Autentica un usuario mediante username/email y contraseña. " +
                           "Retorna un token JWT válido por 24 horas.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                    description = "Login exitoso - Token JWT generado",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", 
                    description = "Credenciales inválidas",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<?> login(
            @Valid @RequestBody 
            @Parameter(description = "Credenciales de acceso (username/email y contraseña)") 
            LoginRequest request) {
        try {
            // Autenticar usuario
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Generar JWT
            String jwt = jwtUtil.generateToken(authentication);
            
            // Obtener información del usuario
            Usuario usuario = (Usuario) authentication.getPrincipal();
            UsuarioResponse usuarioResponse = new UsuarioResponse();
            usuarioResponse.setId(usuario.getId());
            usuarioResponse.setUsername(usuario.getUsername());
            usuarioResponse.setEmail(usuario.getEmail());
            usuarioResponse.setFirstName(usuario.getFirstName());
            usuarioResponse.setLastName(usuario.getLastName());
            usuarioResponse.setRole(usuario.getRole());
            usuarioResponse.setEnabled(usuario.getEnabled());
            usuarioResponse.setCreatedAt(usuario.getCreatedAt());
            usuarioResponse.setUpdatedAt(usuario.getUpdatedAt());
            
            LoginResponse loginResponse = new LoginResponse(jwt, usuarioResponse);
            
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Credenciales inválidas");
            errorResponse.put("message", "Verifique su username/email y contraseña");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
    
    @GetMapping("/perfil")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Obtener perfil del usuario autenticado",
               description = "Devuelve la información del usuario que está actualmente autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                    description = "Perfil obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
        @ApiResponse(responseCode = "401", 
                    description = "Token JWT inválido o expirado")
    })
    public ResponseEntity<UsuarioResponse> obtenerPerfil(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        UsuarioResponse response = usuarioService.obtenerUsuarioPorId(usuario.getId());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Obtener todos los usuarios", 
               description = "Devuelve una lista completa de todos los usuarios registrados. Solo disponible para administradores.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                    description = "Lista de usuarios obtenida exitosamente"),
        @ApiResponse(responseCode = "401", 
                    description = "Token JWT requerido"),
        @ApiResponse(responseCode = "403", 
                    description = "Acceso denegado - Se requiere rol de ADMIN")
    })
    public ResponseEntity<List<UsuarioResponse>> obtenerTodosLosUsuarios() {
        List<UsuarioResponse> usuarios = usuarioService.obtenerTodosLosUsuarios();
        return ResponseEntity.ok(usuarios);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Obtener usuario por ID",
               description = "Obtiene la información de un usuario específico por su ID. " +
                           "Los usuarios solo pueden ver su propia información, los admin pueden ver cualquiera.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                    description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
        @ApiResponse(responseCode = "404", 
                    description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "403", 
                    description = "Sin permisos para ver este usuario")
    })
    public ResponseEntity<UsuarioResponse> obtenerUsuarioPorId(
            @Parameter(description = "ID único del usuario", example = "1") 
            @PathVariable Long id) {
        try {
            UsuarioResponse usuario = usuarioService.obtenerUsuarioPorId(id);
            return ResponseEntity.ok(usuario);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Obtener usuarios por rol", 
               description = "Filtra usuarios por su rol (ADMIN o CUSTOMER). Solo disponible para administradores.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                    description = "Lista de usuarios filtrada por rol"),
        @ApiResponse(responseCode = "403", 
                    description = "Acceso denegado - Se requiere rol de ADMIN")
    })
    public ResponseEntity<List<UsuarioResponse>> obtenerUsuariosPorRole(
            @Parameter(description = "Rol del usuario (ADMIN o CUSTOMER)", example = "CUSTOMER") 
            @PathVariable Role role) {
        List<UsuarioResponse> usuarios = usuarioService.obtenerUsuariosPorRole(role);
        return ResponseEntity.ok(usuarios);
    }
    
    @GetMapping("/buscar")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Buscar usuarios", 
               description = "Busca usuarios por nombre o apellido (búsqueda parcial, no sensible a mayúsculas)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                    description = "Resultados de búsqueda obtenidos"),
        @ApiResponse(responseCode = "403", 
                    description = "Acceso denegado - Se requiere rol de ADMIN")
    })
    public ResponseEntity<List<UsuarioResponse>> buscarUsuarios(
            @RequestParam 
            @Parameter(description = "Término de búsqueda (nombre o apellido)", example = "Juan") 
            String q) {
        List<UsuarioResponse> usuarios = usuarioService.buscarUsuarios(q);
        return ResponseEntity.ok(usuarios);
    }
    
    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Obtener estadísticas de usuarios", 
               description = "Proporciona estadísticas generales del sistema: total de usuarios, por roles, etc.")
    @ApiResponse(responseCode = "200", 
                description = "Estadísticas obtenidas exitosamente")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsuarios", usuarioService.obtenerTodosLosUsuarios().size());
        stats.put("administradores", usuarioService.contarUsuariosPorRole(Role.ADMIN));
        stats.put("compradores", usuarioService.contarUsuariosPorRole(Role.CUSTOMER));
        stats.put("usuariosActivos", usuarioService.obtenerUsuariosActivos().size());
        
        return ResponseEntity.ok(stats);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Actualizar usuario",
               description = "Actualiza la información de un usuario. Los usuarios pueden actualizar su propia información, los admin pueden actualizar cualquiera.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                    description = "Usuario actualizado exitosamente"),
        @ApiResponse(responseCode = "400", 
                    description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", 
                    description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "403", 
                    description = "Sin permisos para actualizar este usuario")
    })
    public ResponseEntity<?> actualizarUsuario(
            @Parameter(description = "ID del usuario a actualizar") @PathVariable Long id,
            @Valid @RequestBody @Parameter(description = "Nuevos datos del usuario") RegistroUsuarioRequest request) {
        try {
            UsuarioResponse usuarioActualizado = usuarioService.actualizarUsuario(id, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Usuario actualizado exitosamente");
            response.put("usuario", usuarioActualizado);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Cambiar estado del usuario", 
               description = "Activa o desactiva un usuario. Solo disponible para administradores.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                    description = "Estado cambiado exitosamente"),
        @ApiResponse(responseCode = "404", 
                    description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "403", 
                    description = "Acceso denegado - Se requiere rol de ADMIN")
    })
    public ResponseEntity<Map<String, String>> cambiarEstadoUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @RequestParam @Parameter(description = "Nuevo estado (true=activo, false=inactivo)", example = "true") Boolean enabled) {
        try {
            usuarioService.cambiarEstadoUsuario(id, enabled);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Estado del usuario actualizado exitosamente");
            response.put("nuevoEstado", enabled ? "Activo" : "Inactivo");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Eliminar usuario", 
               description = "Elimina permanentemente un usuario del sistema. Solo disponible para administradores. ¡CUIDADO: Esta acción no se puede deshacer!")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                    description = "Usuario eliminado exitosamente"),
        @ApiResponse(responseCode = "404", 
                    description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "403", 
                    description = "Acceso denegado - Se requiere rol de ADMIN")
    })
    public ResponseEntity<Map<String, String>> eliminarUsuario(
            @Parameter(description = "ID del usuario a eliminar") @PathVariable Long id) {
        try {
            usuarioService.eliminarUsuario(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Usuario eliminado exitosamente");
            response.put("id", id.toString());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}