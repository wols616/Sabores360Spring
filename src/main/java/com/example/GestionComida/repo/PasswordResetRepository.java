package com.example.GestionComida.repo;

import com.example.GestionComida.domain.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, String> {}
