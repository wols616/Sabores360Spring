package com.example.GestionComida.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "password_resets")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PasswordReset {
    @Id
    @Column(length=255)
    private String email;

    @Column(nullable=false, length=255)
    private String token;

    @Column(name="created_at", insertable=false, updatable=false)
    private Instant createdAt;
}
