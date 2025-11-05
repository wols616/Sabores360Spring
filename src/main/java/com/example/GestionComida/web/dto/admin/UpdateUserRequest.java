package com.example.GestionComida.web.dto.admin;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    
    @Email(message = "El email debe ser válido")
    private String email;
    
    private String password; // Si se proporciona, se actualiza
    private String address;
    private Integer roleId;
    private Boolean isActive;
}
