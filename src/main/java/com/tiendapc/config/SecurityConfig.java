package com.tiendapc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint unauthorizedHandler;

    public SecurityConfig(JwtAuthenticationEntryPoint unauthorizedHandler) {
        this.unauthorizedHandler = unauthorizedHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            // CORS - Permitir acceso desde el frontend
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // CSRF - Deshabilitado para APIs REST
            .csrf(csrf -> csrf.disable())
            
            // Manejo de excepciones
            .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
            
            // Sin sesiones - Stateless JWT
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Autorización de endpoints
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos - Sin autenticación
                .requestMatchers(
                    "/api/usuarios/registro",
                    "/api/usuarios/login",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/api-docs/**",
                    "/actuator/**",
                    "/",
                    "/error",
                    "/test",
                    "/api/test"
                ).permitAll()
                
                // Endpoints de administración - Solo ADMIN
                .requestMatchers(
                    "/api/usuarios/estadisticas",
                    "/api/usuarios/role/**",
                    "/api/usuarios/buscar"
                ).hasRole("ADMIN")
                
                // Todos los demás endpoints requieren autenticación
                .anyRequest().authenticated()
            )
            
            // Deshabilitar login form y basic auth
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            
            // Permitir H2 Console en desarrollo (si se usa)
            .headers(h -> h.frameOptions(frame -> frame.disable()));

        // Agregar filtro JWT antes del filtro de autenticación de Spring
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    /**
     * Configuración CORS para permitir acceso desde el frontend
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir estos orígenes (frontend)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5500",      // Live Server VSCode
            "http://127.0.0.1:5500",      // Live Server alternativo
            "http://localhost:3000",      // React (si se migra)
            "http://localhost:8080",      // Nginx/Gateway
            "http://localhost:4200"       // Angular (si se migra)
        ));
        
        // Permitir todos los métodos HTTP
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Permitir todos los headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Permitir credenciales (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Exponer estos headers al frontend
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Total-Count"
        ));
        
        // Tiempo de cache para preflight requests (1 hora)
        configuration.setMaxAge(3600L);
        
        // Aplicar configuración a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}