package com.example.GestionComida.web.dto.admin;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductRequest {
    private String name;
    private String description;
    
    @Positive(message = "El precio debe ser positivo")
    private BigDecimal price;
    
    private Integer stock;
    private Integer categoryId;
    private String imageUrl;
    private Boolean isAvailable;
}
