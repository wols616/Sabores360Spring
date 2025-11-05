package com.example.GestionComida.web.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
    
    private String description;
    
    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser positivo")
    private BigDecimal price;
    
    @NotNull(message = "El stock es obligatorio")
    private Integer stock;
    
    @NotNull(message = "La categoría es obligatoria")
    private Integer categoryId;
    
    private String imageUrl;
    
    private Boolean isAvailable = true;
}
