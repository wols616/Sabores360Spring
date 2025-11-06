package com.example.GestionComida.web.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancellationReasonDto {
    private String reason;
    private Long count;
}
