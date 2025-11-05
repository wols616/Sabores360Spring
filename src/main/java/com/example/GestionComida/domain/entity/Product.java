package com.example.GestionComida.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
        import lombok.*;
        import java.time.Instant;
import java.math.BigDecimal;

@Entity @Table(name = "products")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional=false) @JoinColumn(name="category_id")
    private Category category;

    @Column(nullable=false, length=255)
    private String name;

    @Lob
    private String description;

    @Column(nullable=false, precision=10, scale=2)
    private BigDecimal price;

    @Column(nullable=false)
    private Integer stock;

    @Column(name="image_url")
    private String imageUrl;

    @Column(name="is_available", nullable=false)
    private Boolean isAvailable = true;

    @Column(name="created_at", insertable=false, updatable=false)
    private Instant createdAt;

    @Column(name="updated_at", insertable=false, updatable=false)
    private Instant updatedAt;
}
