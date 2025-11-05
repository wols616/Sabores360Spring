package com.example.GestionComida.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="users")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional=false) @JoinColumn(name="role_id")
    private Role role;

    @Column(nullable=false, length=255)
    private String name;

    @Column(nullable=false, unique=true, length=255)
    private String email;

    @Column(name="password_hash", nullable=false, length=255)
    private String passwordHash;

    @Lob
    private String address;

    @Column(name="is_active", nullable=false)
    private Boolean isActive = true;

    @Column(name="created_at", updatable=false, insertable=false)
    private Instant createdAt;

    @Column(name="updated_at", insertable=false, updatable=false)
    private Instant updatedAt;
}

