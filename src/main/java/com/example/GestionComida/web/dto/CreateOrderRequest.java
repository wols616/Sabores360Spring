// src/main/java/com/example/GestionComida/web/dto/CreateOrderRequest.java
package com.example.GestionComida.web.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class CreateOrderRequest {
    @NotNull
    private Integer clientId;

    @NotBlank
    private String deliveryAddress;

    @NotBlank
    @Pattern(regexp = "Tarjeta|Efectivo")
    private String paymentMethod;

    @NotEmpty
    private List<Item> items;

    public CreateOrderRequest() {}

    public CreateOrderRequest(Integer clientId, String deliveryAddress, String paymentMethod, List<Item> items) {
        this.clientId = clientId;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.items = items;
    }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    // ---- Ítems del pedido ----
    public static class Item {
        @NotNull
        private Integer productId;

        @Positive
        private int quantity;

        public Item() {}

        public Item(Integer productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public Integer getProductId() { return productId; }
        public void setProductId(Integer productId) { this.productId = productId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
