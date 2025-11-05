package com.example.GestionComida.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderDetailDto {
    private Integer id;
    private String status;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private String paymentMethod;
    private Instant createdAt;
    private List<OrderItemDto> items;

    public OrderDetailDto() {}
    public OrderDetailDto(Integer id, String status, BigDecimal totalAmount, String deliveryAddress,
                          String paymentMethod, Instant createdAt, List<OrderItemDto> items) {
        this.id = id;
        this.status = status;
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
        this.items = items;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }
}
