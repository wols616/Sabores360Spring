package com.example.GestionComida.repo;

import com.example.GestionComida.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {}
