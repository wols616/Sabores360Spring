package com.example.GestionComida.repo;

import com.example.GestionComida.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByClient_Id(Integer clientId);
    List<Order> findBySeller_Id(Integer sellerId);

    // Native query to replicate the PHP logic: GROUP BY DATE(created_at), exclude cancelled/anulado
    @Query(value = "SELECT DATE(created_at) AS date, COALESCE(SUM(total_amount),0) AS total " +
            "FROM orders " +
            "WHERE created_at BETWEEN :start AND :end " +
            "AND LOWER(TRIM(status)) NOT IN ('cancelado','anulado') " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date",
            nativeQuery = true)
    List<SalesByDayProjection> findSalesByDay(@Param("start") Timestamp start, @Param("end") Timestamp end);
}
