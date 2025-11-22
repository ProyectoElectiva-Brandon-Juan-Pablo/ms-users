package com.tiendapc.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthBeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    // IMPORTANTE: esto es SOLO temporal para obtener el hash en consola
@Bean
public CommandLineRunner printAdminPasswordHash(PasswordEncoder passwordEncoder) {
    return args -> {
        String raw = "admin123";
        String encoded = passwordEncoder.encode(raw);
        System.out.println("==== HASH PARA admin123 ====");
        System.out.println(encoded);
        System.out.println("==== FIN HASH ====");
    };
}

}
