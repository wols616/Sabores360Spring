package com.example.GestionComida.security;

import com.example.GestionComida.domain.entity.User;
import com.example.GestionComida.repo.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                String email = jwtUtil.extractEmail(token);
                
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    User user = userRepository.findByEmail(email).orElse(null);
                    
                    if (user != null && jwtUtil.validateToken(token, email)) {
                        String role = "ROLE_" + user.getRole().getName().toUpperCase();
                        
                        UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority(role))
                            );
                        
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                // Token inválido - continuar sin autenticación
                logger.warn("Error procesando token JWT: " + e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
