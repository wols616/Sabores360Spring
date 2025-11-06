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
import jakarta.persistence.Tuple;
import java.sql.Date;
import java.time.LocalDate;
import com.example.GestionComida.web.dto.report.UsersByDayDto;
import com.example.GestionComida.web.dto.report.StatusCountDto;
import com.example.GestionComida.web.dto.report.GenericCountDto;
import com.example.GestionComida.web.dto.report.RateDto;
import com.example.GestionComida.web.dto.report.CancellationReasonDto;

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

    /** Usuarios nuevos por día */
    public List<UsersByDayDto> usuariosPorDia(LocalDate from, LocalDate to){
        String q =
                "SELECT FUNCTION('DATE', u.createdAt) AS fecha, " +
                        "       COUNT(u.id) AS usuarios " +
                        "FROM User u " +
                        "WHERE FUNCTION('DATE', u.createdAt) BETWEEN :from AND :to " +
                        "GROUP BY FUNCTION('DATE', u.createdAt) " +
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
            Long usuarios = (Long) t.get("usuarios");
            return new UsersByDayDto(fecha, usuarios);
        }).collect(Collectors.toList());
    }

    /** Conteo de pedidos por estado en el rango */
    public List<StatusCountDto> pedidosPorEstado(LocalDate from, LocalDate to){
        String q =
                "SELECT o.status AS status, COUNT(o.id) AS cnt " +
                        "FROM Order o " +
                        "WHERE FUNCTION('DATE', o.updatedAt) BETWEEN :from AND :to " +
                        "GROUP BY o.status";
        List<Tuple> tuples = em.createQuery(q, Tuple.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        return tuples.stream().map(t -> {
            String status = (String) t.get("status");
            Long cnt = (Long) t.get("cnt");
            return new StatusCountDto(status, cnt);
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

    /** Serie de conteos según granularidad: daily/weekly/monthly. Devuelve puntos con label y count */
    public List<GenericCountDto> ordersSeries(LocalDate from, LocalDate to, String granularity){
        String groupExpr;
        String labelExpr;
        if ("weekly".equalsIgnoreCase(granularity)){
            // Use YEAR and WEEK
            groupExpr = "FUNCTION('YEAR', o.updatedAt), FUNCTION('WEEK', o.updatedAt)";
            labelExpr = "CONCAT(FUNCTION('YEAR', o.updatedAt), '-', FUNCTION('WEEK', o.updatedAt))";
        } else if ("monthly".equalsIgnoreCase(granularity)){
            groupExpr = "FUNCTION('YEAR', o.updatedAt), FUNCTION('MONTH', o.updatedAt)";
            labelExpr = "CONCAT(FUNCTION('YEAR', o.updatedAt), '-', LPAD(CAST(FUNCTION('MONTH', o.updatedAt) AS string),2,'0'))";
        } else {
            groupExpr = "FUNCTION('DATE', o.updatedAt)";
            labelExpr = "FUNCTION('DATE', o.updatedAt)";
        }

        String q = "SELECT " + labelExpr + " AS label, COUNT(o.id) AS cnt " +
                "FROM Order o " +
                "WHERE FUNCTION('DATE', o.updatedAt) BETWEEN :from AND :to " +
                "GROUP BY " + groupExpr + " ORDER BY label";

        List<Tuple> tuples = em.createQuery(q, Tuple.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        return tuples.stream().map(t -> {
            Object lbl = t.get("label");
            String label = lbl == null ? null : lbl.toString();
            Long cnt = (Long) t.get("cnt");
            return new GenericCountDto(label, cnt);
        }).collect(Collectors.toList());
    }

    /** Conteo total de pedidos en rango */
    public Long ordersTotal(LocalDate from, LocalDate to){
        String q = "SELECT COUNT(o.id) FROM Order o WHERE FUNCTION('DATE', o.updatedAt) BETWEEN :from AND :to";
        Long v = (Long) em.createQuery(q, Long.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
        return v;
    }

    /** Tasa y conteos para confirmacion/closure/cancel */
    public RateDto confirmationRate(LocalDate from, LocalDate to){
        Long total = ordersTotal(from, to);
        String q = "SELECT COUNT(o.id) FROM Order o WHERE o.status = 'Confirmado' AND FUNCTION('DATE', o.updatedAt) BETWEEN :from AND :to";
        Long confirmed = (Long) em.createQuery(q, Long.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
        double pct = total == 0 ? 0.0 : (confirmed.doubleValue() * 100.0 / total.doubleValue());
        return new RateDto("confirmation", pct, confirmed, total);
    }

    public RateDto closureRate(LocalDate from, LocalDate to){
        Long total = ordersTotal(from, to);
        String q = "SELECT COUNT(o.id) FROM Order o WHERE o.status = 'Entregado' AND FUNCTION('DATE', o.updatedAt) BETWEEN :from AND :to";
        Long closed = (Long) em.createQuery(q, Long.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
        double pct = total == 0 ? 0.0 : (closed.doubleValue() * 100.0 / total.doubleValue());
        return new RateDto("closure", pct, closed, total);
    }

    public RateDto cancellationRate(LocalDate from, LocalDate to){
        Long total = ordersTotal(from, to);
        String q = "SELECT COUNT(o.id) FROM Order o WHERE o.status = 'Cancelado' AND FUNCTION('DATE', o.updatedAt) BETWEEN :from AND :to";
        Long canceled = (Long) em.createQuery(q, Long.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
        double pct = total == 0 ? 0.0 : (canceled.doubleValue() * 100.0 / total.doubleValue());
        return new RateDto("cancellation", pct, canceled, total);
    }

    /** Cancellation reasons breakdown using OrderStatusHistory.notes when status=Cancelado */
    public List<CancellationReasonDto> cancellationReasons(LocalDate from, LocalDate to){
        String q = "SELECT h.notes AS reason, COUNT(h.id) AS cnt FROM OrderStatusHistory h " +
                "WHERE h.status = 'Cancelado' AND FUNCTION('DATE', h.changedAt) BETWEEN :from AND :to " +
                "GROUP BY h.notes ORDER BY cnt DESC";
        List<Tuple> tuples = em.createQuery(q, Tuple.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        return tuples.stream().map(t -> {
            String reason = (String) t.get("reason");
            Long cnt = (Long) t.get("cnt");
            if (reason == null) reason = "unspecified";
            return new CancellationReasonDto(reason, cnt);
        }).collect(Collectors.toList());
    }

    /** Revenue totals and comparisons */
    public BigDecimal revenueTotal(LocalDate from, LocalDate to){
        String q = "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'Entregado' AND FUNCTION('DATE', o.updatedAt) BETWEEN :from AND :to";
        BigDecimal v = (BigDecimal) em.createQuery(q, BigDecimal.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
        return v == null ? BigDecimal.ZERO : v;
    }

    /** Revenue by seller */
    public List<SalesBySellerDto> revenueBySeller(LocalDate from, LocalDate to){
        return ventasPorVendedor(from, to);
    }

    /** Revenue by payment method (channel) */
    public List<GenericCountDto> revenueByChannel(LocalDate from, LocalDate to){
        String q = "SELECT o.paymentMethod AS label, SUM(o.totalAmount) AS total FROM Order o WHERE o.status = 'Entregado' AND FUNCTION('DATE', o.updatedAt) BETWEEN :from AND :to GROUP BY o.paymentMethod ORDER BY total DESC";
        List<Tuple> tuples = em.createQuery(q, Tuple.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        return tuples.stream().map(t -> new GenericCountDto((t.get("label") == null ? "unknown" : t.get("label").toString()), ((Number)t.get("total")).longValue())).collect(Collectors.toList());
    }

    /** Revenue by product category */
    public List<GenericCountDto> revenueByCategory(LocalDate from, LocalDate to){
        String q = "SELECT i.product.category.name AS label, SUM(i.unitPrice * i.quantity) AS total " +
                "FROM OrderItem i JOIN i.order o WHERE o.status='Entregado' AND FUNCTION('DATE', o.updatedAt) BETWEEN :from AND :to GROUP BY i.product.category.name ORDER BY total DESC";
        List<Tuple> tuples = em.createQuery(q, Tuple.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        return tuples.stream().map(t -> new GenericCountDto((t.get("label") == null ? "unknown" : t.get("label").toString()), ((Number)t.get("total")).longValue())).collect(Collectors.toList());
    }

    /** Clients ranked by number of orders */
    public List<GenericCountDto> clientsByOrders(LocalDate from, LocalDate to, int limit){
        String q = "SELECT o.client.id AS cid, o.client.name AS name, COUNT(o.id) AS cnt, SUM(o.totalAmount) AS total " +
                "FROM Order o WHERE FUNCTION('DATE', o.updatedAt) BETWEEN :from AND :to GROUP BY o.client.id, o.client.name ORDER BY cnt DESC";
        List<Tuple> tuples = em.createQuery(q, Tuple.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .setMaxResults(limit)
                .getResultList();
        return tuples.stream().map(t -> new GenericCountDto((t.get("name") == null ? t.get("cid").toString() : t.get("name").toString()), (Long) t.get("cnt"))).collect(Collectors.toList());
    }
}
