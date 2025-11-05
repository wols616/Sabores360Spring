package com.example.GestionComida.web.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopProductDto {
    private Integer productoId;
    private String productoNombre;
    private Long cantidadVendida;
    private BigDecimal totalVentas;
}
