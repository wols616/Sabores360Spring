package com.example.GestionComida.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Allow preflight CORS requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Actuator (permitir temporalmente para depuración / frontend)
                .requestMatchers("/actuator/**").permitAll()
                // Endpoints públicos (no requieren autenticación)
                .requestMatchers("/api/public/**").permitAll()
                // Permitir acceso público al detalle público de pedidos
                .requestMatchers("/api/orders/*/details").permitAll()
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
        // Desarrollo: permitir cualquier origen/puerto.
        // Usamos allowed origin patterns para aceptar cualquier origen (incluyendo distinto puerto)
        // y mantener allowCredentials=true; en producción restringe esto a orígenes concretos.
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        // Permitir todos los métodos en desarrollo
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
