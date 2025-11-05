package com.example.GestionComida.web.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
    
    private String address;
    
    @NotNull(message = "El rol es obligatorio")
    private Integer roleId;
    
    private Boolean isActive = true;
}
