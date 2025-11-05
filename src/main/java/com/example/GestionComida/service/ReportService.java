package com.example.GestionComida.service;

import com.example.GestionComida.web.dto.report.SalesByDayDto;
import com.example.GestionComida.web.dto.report.SalesBySellerDto;
import com.example.GestionComida.web.dto.report.TopProductDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final EntityManager em;

    /** Ventas por día (solo Entregado) */
    public List<SalesByDayDto> ventasPorDia(LocalDate from, LocalDate to){
        String q =
                "SELECT FUNCTION('DATE', o.updatedAt) AS fecha, " +
                        "       SUM(o.totalAmount) AS total, " +
                        "       COUNT(o.id) AS pedidos " +
                        "FROM Order o " +
                        "WHERE o.status = 'Entregado' " +
                        "  AND FUNCTION('DATE', o.updatedAt) BETWEEN :from AND :to " +
                        "GROUP BY FUNCTION('DATE', o.updatedAt) " +
                        "ORDER BY fecha";
        List<Tuple> tuples = em.createQuery(q, Tuple.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        
        return tuples.stream().map(t -> {
            Object fechaObj = t.get("fecha");
            LocalDate fecha = null;
            if (fechaObj instanceof Date) {
                fecha = ((Date) fechaObj).toLocalDate();
            } else if (fechaObj instanceof LocalDate) {
                fecha = (LocalDate) fechaObj;
            }
            BigDecimal total = (BigDecimal) t.get("total");
            Long pedidos = (Long) t.get("pedidos");
            return new SalesByDayDto(fecha, pedidos, total);
        }).collect(Collectors.toList());
    }

    /** Ventas por vendedor (solo Entregado) */
    public List<SalesBySellerDto> ventasPorVendedor(LocalDate from, LocalDate to){
        String q =
                "SELECT o.seller.id AS vendedorId, " +
                        "       o.seller.name AS vendedorNombre, " +
                        "       SUM(o.totalAmount) AS total, " +
                        "       COUNT(o.id) AS pedidos " +
                        "FROM Order o " +
                        "WHERE o.status = 'Entregado' " +
                        "  AND FUNCTION('DATE', o.updatedAt) BETWEEN :from AND :to " +
                        "  AND o.seller IS NOT NULL " +
                        "GROUP BY o.seller.id, o.seller.name " +
                        "ORDER BY total DESC";
        List<Tuple> tuples = em.createQuery(q, Tuple.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        
        return tuples.stream().map(t -> {
            Integer vendedorId = (Integer) t.get("vendedorId");
            String vendedorNombre = (String) t.get("vendedorNombre");
            BigDecimal total = (BigDecimal) t.get("total");
            Long pedidos = (Long) t.get("pedidos");
            return new SalesBySellerDto(vendedorId, vendedorNombre, pedidos, total);
        }).collect(Collectors.toList());
    }

    /** Top productos por cantidad vendida (solo Entregado) */
    public List<TopProductDto> topProductos(LocalDate from, LocalDate to, int limit){
        String q =
                "SELECT i.product.id AS productoId, " +
                        "       i.product.name AS nombre, " +
                        "       SUM(i.quantity) AS cantidad, " +
                        "       SUM(i.unitPrice * i.quantity) AS total " +
                        "FROM OrderItem i " +
                        "JOIN i.order o " +
                        "WHERE o.status = 'Entregado' " +
                        "  AND FUNCTION('DATE', o.updatedAt) BETWEEN :from AND :to " +
                        "GROUP BY i.product.id, i.product.name " +
                        "ORDER BY cantidad DESC";
        List<Tuple> tuples = em.createQuery(q, Tuple.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .setMaxResults(Math.max(limit, 1))
                .getResultList();
        
        return tuples.stream().map(t -> {
            Integer productoId = (Integer) t.get("productoId");
            String nombre = (String) t.get("nombre");
            Long cantidad = (Long) t.get("cantidad");
            BigDecimal total = (BigDecimal) t.get("total");
            return new TopProductDto(productoId, nombre, cantidad, total);
        }).collect(Collectors.toList());
    }
}
