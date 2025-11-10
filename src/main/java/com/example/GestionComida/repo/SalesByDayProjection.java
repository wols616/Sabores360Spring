package com.example.GestionComida.repo;

import java.math.BigDecimal;

/**
 * Projection for sales by day native query.
 */
public interface SalesByDayProjection {
    // date in format YYYY-MM-DD as returned by DATE(created_at)
    String getDate();
    BigDecimal getTotal();
}
