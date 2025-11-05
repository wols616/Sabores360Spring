package com.example.GestionComida.web.dto.seller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BulkStockRequest {
    @NotEmpty
    private List<Item> items;

    public BulkStockRequest() {}
    public BulkStockRequest(List<Item> items) { this.items = items; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public static class Item {
        @NotNull private Integer id;
        @NotNull private Integer stock;

        public Item() {}
        public Item(Integer id, Integer stock) { this.id = id; this.stock = stock; }

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
    }
}
