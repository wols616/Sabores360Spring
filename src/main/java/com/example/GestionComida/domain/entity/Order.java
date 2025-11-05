package com.example.GestionComida.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name = "orders")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional=false) @JoinColumn(name="client_id")
    private User client;

    @ManyToOne @JoinColumn(name="seller_id")
    private User seller;

    @Lob @Column(name="delivery_address", nullable=false)
    private String deliveryAddress;

    @Column(name="total_amount", nullable=false, precision=10, scale=2)
    private BigDecimal totalAmount;

    @Column(name="status", nullable=false,
            columnDefinition="ENUM('Pendiente','Confirmado','En preparación','En camino','Entregado','Cancelado')")
    private String status;

    @Column(name="payment_method", nullable=false, columnDefinition="ENUM('Tarjeta','Efectivo')")
    private String paymentMethod;

    @Column(name="created_at", insertable=false, updatable=false)
    private Instant createdAt;

    @Column(name="updated_at", insertable=false, updatable=false)
    private Instant updatedAt;
}
