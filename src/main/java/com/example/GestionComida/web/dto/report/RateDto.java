package com.example.GestionComida.web.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateDto {
    private String name;
    private Double value; // percentage 0..100
    private Long numerator;
    private Long denominator;
}
