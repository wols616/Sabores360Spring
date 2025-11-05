package com.example.GestionComida.repo;

import com.example.GestionComida.domain.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Integer> {
    List<OrderStatusHistory> findByOrder_IdOrderByChangedAtAsc(Integer orderId);
}
