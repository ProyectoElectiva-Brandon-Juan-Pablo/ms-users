package com.tiendapc.service;

import com.tiendapc.dto.*;
import com.tiendapc.model.Role;
import com.tiendapc.model.Usuario;
import com.tiendapc.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService implements UserDetailsService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        return usuario;
    }
    
    public UsuarioResponse registrarUsuario(RegistroUsuarioRequest request) {
        // Validar si el usuario ya existe
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Error: El nombre de usuario ya está en uso!");
        }
        
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Error: El email ya está en uso!");
        }
        
        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setFirstName(request.getFirstName());
        usuario.setLastName(request.getLastName());
        usuario.setRole(request.getRole() != null ? request.getRole() : Role.CUSTOMER);
        
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return convertirAResponse(usuarioGuardado);
    }
    
    public Optional<Usuario> buscarPorUsernameOEmail(String identifier) {
        return usuarioRepository.findByUsernameOrEmail(identifier);
    }
    
    public UsuarioResponse obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
        return convertirAResponse(usuario);
    }
    
    public List<UsuarioResponse> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }
    
    public List<UsuarioResponse> obtenerUsuariosPorRole(Role role) {
        return usuarioRepository.findByRole(role)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }
    
    public List<UsuarioResponse> obtenerUsuariosActivos() {
        return usuarioRepository.findByEnabledTrue()
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }
    
    public UsuarioResponse actualizarUsuario(Long id, RegistroUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
        
        // Validar si el username ya existe (excluyendo el usuario actual)
        if (!usuario.getUsername().equals(request.getUsername()) && 
            usuarioRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Error: El nombre de usuario ya está en uso!");
        }
        
        // Validar si el email ya existe (excluyendo el usuario actual)
        if (!usuario.getEmail().equals(request.getEmail()) && 
            usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Error: El email ya está en uso!");
        }
        
        // Actualizar campos
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setFirstName(request.getFirstName());
        usuario.setLastName(request.getLastName());
        
        // Solo actualizar la contraseña si se proporciona una nueva
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        if (request.getRole() != null) {
            usuario.setRole(request.getRole());
        }
        
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return convertirAResponse(usuarioActualizado);
    }
    
    public void cambiarEstadoUsuario(Long id, Boolean enabled) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
        
        usuario.setEnabled(enabled);
        usuarioRepository.save(usuario);
    }
    
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con id: " + id);
        }
        usuarioRepository.deleteById(id);
    }
    
    public List<UsuarioResponse> buscarUsuarios(String q) {
        return usuarioRepository.buscarPorNombre(q)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }
    
    public long contarUsuariosPorRole(Role role) {
        return usuarioRepository.countByRole(role);
    }
    
    // Método para validar credenciales (usado en el login)
    public boolean validarCredenciales(String identifier, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsernameOrEmail(identifier);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            return passwordEncoder.matches(password, usuario.getPassword()) && usuario.isEnabled();
        }
        return false;
    }
    
    // Método de conversión - ESTE FALTABA
    private UsuarioResponse convertirAResponse(Usuario usuario) {
        UsuarioResponse response = new UsuarioResponse();
        response.setId(usuario.getId());
        response.setUsername(usuario.getUsername());
        response.setEmail(usuario.getEmail());
        response.setFirstName(usuario.getFirstName());
        response.setLastName(usuario.getLastName());
        response.setRole(usuario.getRole());
        response.setEnabled(usuario.getEnabled());
        response.setCreatedAt(usuario.getCreatedAt());
        response.setUpdatedAt(usuario.getUpdatedAt());
        return response;
    }
}