package com.example.GestionComida.web.dto;

import jakarta.validation.constraints.NotNull;

public class AssignSellerRequest {
    @NotNull
    private Integer sellerId;

    public AssignSellerRequest() {}

    public AssignSellerRequest(Integer sellerId) {
        this.sellerId = sellerId;
    }

    public Integer getSellerId() { return sellerId; }
    public void setSellerId(Integer sellerId) { this.sellerId = sellerId; }
}
