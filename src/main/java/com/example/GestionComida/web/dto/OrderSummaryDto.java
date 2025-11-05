package com.example.GestionComida.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class OrderSummaryDto {
    private Integer id;
    private String status;
    private BigDecimal totalAmount;
    private Instant createdAt;

    public OrderSummaryDto() {}
    public OrderSummaryDto(Integer id, String status, BigDecimal totalAmount, Instant createdAt) {
        this.id = id;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
