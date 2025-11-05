package com.example.GestionComida.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Habilitar CORS y luego deshabilitar CSRF (API stateless)
            .cors().and()
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Actuator (permitir temporalmente para depuración / frontend)
                .requestMatchers("/actuator/**").permitAll()
                // Endpoints públicos de autenticación
                .requestMatchers("/api/auth/**").permitAll()
                
                // Endpoints de cliente - requieren rol CLIENTE
                .requestMatchers("/api/client/**").hasRole("CLIENTE")
                
                // Endpoints de vendedor - requieren rol VENDEDOR
                .requestMatchers("/api/seller/**").hasRole("VENDEDOR")
                
                // Endpoints de administrador - requieren rol ADMINISTRADOR
                .requestMatchers("/api/admin/**").hasRole("ADMINISTRADOR")
                
                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Cors configuration source para desarrollo.
     * Permite el origen http://localhost:8888 y los métodos/headers necesarios.
     * En producción ajusta esto para restringir orígenes.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // Orígenes permitidos en desarrollo. Reemplaza o añade dominios según necesites.
        config.addAllowedOrigin("http://localhost:8888");
        config.addAllowedHeader("*");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
