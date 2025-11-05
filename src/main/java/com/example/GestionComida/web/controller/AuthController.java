// src/main/java/com/example/GestionComida/web/controller/AuthController.java
package com.example.GestionComida.web.controller;

import com.example.GestionComida.service.AuthService;
import com.example.GestionComida.web.ApiResponse;
import com.example.GestionComida.web.dto.auth.ForgotPasswordRequest;
import com.example.GestionComida.web.dto.auth.LoginRequest;
import com.example.GestionComida.web.dto.auth.RegisterRequest;
import com.example.GestionComida.web.dto.auth.ResetPasswordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    @PostMapping("/login")
    public Map<String,Object> login(@RequestBody @Valid LoginRequest req){
        Map<String,Object> result = auth.login(req.getEmail(), req.getPassword());
        result.put("success", true);
        return result;
    }

    @GetMapping("/me")
    public Map<String,Object> me(org.springframework.security.core.Authentication authentication) {
        Map<String,Object> response = new java.util.HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null || !(authentication.getPrincipal() instanceof com.example.GestionComida.domain.entity.User)) {
            response.put("success", true);
            response.put("user", null);
            return response;
        }

        com.example.GestionComida.domain.entity.User u = (com.example.GestionComida.domain.entity.User) authentication.getPrincipal();
        Map<String,Object> userMap = new java.util.HashMap<>();
        userMap.put("id", u.getId());
        userMap.put("name", u.getName());
        userMap.put("email", u.getEmail());
        if (u.getRole() != null && u.getRole().getName() != null) {
            String rn = u.getRole().getName().toLowerCase(java.util.Locale.ROOT);
            if ("administrador".equals(rn)) userMap.put("role", "admin");
            else if ("vendedor".equals(rn)) userMap.put("role", "seller");
            else userMap.put("role", "client");
        }

        response.put("success", true);
        response.put("user", userMap);
        return response;
    }

    @PostMapping("/register")
    public Map<String,Object> register(@RequestBody @Valid RegisterRequest req){
        int id = auth.register(req.getName(), req.getEmail(), req.getAddress(), req.getPassword());
        Map<String,Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", id);
        return response;
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgot(@RequestBody @Valid ForgotPasswordRequest req){
        auth.forgotPassword(req.getEmail());
        return ApiResponse.ok();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> reset(@RequestBody @Valid ResetPasswordRequest req){
        auth.resetPassword(req.getToken(), req.getPassword());
        return ApiResponse.ok();
    }

    @RequestMapping(value="/logout", method={RequestMethod.GET, RequestMethod.POST})
    public ApiResponse<Void> logout(){
        // Si implementas JWT real, aquí podrías agregar blacklist/expiración.
        return ApiResponse.ok();
    }
}
