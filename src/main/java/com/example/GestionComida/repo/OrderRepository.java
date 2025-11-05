package com.example.GestionComida.repo;

import com.example.GestionComida.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByClient_Id(Integer clientId);
    List<Order> findBySeller_Id(Integer sellerId);
}
