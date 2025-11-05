package com.example.GestionComida.repo;

import com.example.GestionComida.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    List<OrderItem> findByOrder_Id(Integer orderId);
}
