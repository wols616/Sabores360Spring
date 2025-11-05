package com.example.GestionComida.web.dto.client;

import jakarta.validation.constraints.*;
import java.util.List;

public class PlaceOrderRequest {
    @NotBlank private String delivery_address;
    @NotBlank @Pattern(regexp = "Tarjeta|Efectivo") private String payment_method;
    @NotNull @NotEmpty private List<CartItem> cart;

    public PlaceOrderRequest() {}
    public PlaceOrderRequest(String delivery_address, String payment_method, List<CartItem> cart){
        this.delivery_address = delivery_address; this.payment_method = payment_method; this.cart = cart;
    }

    public String getDelivery_address(){ return delivery_address; }
    public void setDelivery_address(String delivery_address){ this.delivery_address = delivery_address; }
    public String getPayment_method(){ return payment_method; }
    public void setPayment_method(String payment_method){ this.payment_method = payment_method; }
    public List<CartItem> getCart(){ return cart; }
    public void setCart(List<CartItem> cart){ this.cart = cart; }

    public static class CartItem {
        @NotNull private Integer id;
        @Positive private int quantity;

        public CartItem() {}
        public CartItem(Integer id, int quantity){ this.id = id; this.quantity = quantity; }

        public Integer getId(){ return id; }
        public void setId(Integer id){ this.id = id; }
        public int getQuantity(){ return quantity; }
        public void setQuantity(int quantity){ this.quantity = quantity; }
    }
}
