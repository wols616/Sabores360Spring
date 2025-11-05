package com.example.GestionComida.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "order_status_history")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional=false) @JoinColumn(name="order_id")
    private Order order;

    @Column(name="status", nullable=false,
            columnDefinition="ENUM('Pendiente','Confirmado','En preparación','En camino','Entregado','Cancelado')")
    private String status;

    @Column(name="changed_at", insertable=false, updatable=false)
    private Instant changedAt;

    @Lob
    private String notes;
}
