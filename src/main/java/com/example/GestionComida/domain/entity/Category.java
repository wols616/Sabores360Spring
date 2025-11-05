package com.example.GestionComida.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "categories")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable=false, unique=true, length=100)
    private String name;

    @Lob
    private String description;

    @Column(name="created_at", insertable=false, updatable=false)
    private Instant createdAt;
}
