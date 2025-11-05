package com.example.GestionComida.web.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesByDayDto {
    private LocalDate fecha;
    private Long cantidadPedidos;
    private BigDecimal totalVentas;
}
