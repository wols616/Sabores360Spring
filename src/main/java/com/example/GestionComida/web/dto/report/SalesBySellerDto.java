package com.example.GestionComida.web.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesBySellerDto {
    private Integer vendedorId;
    private String vendedorNombre;
    private Long cantidadPedidos;
    private BigDecimal totalVentas;
}
