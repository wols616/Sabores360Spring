package com.example.GestionComida.web.dto;

import java.math.BigDecimal;

public class ProductListDto {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Boolean isAvailable;
    private Integer categoryId;
    private String categoryName;

    public ProductListDto() {}
    public ProductListDto(Integer id, String name, String description, BigDecimal price,
                          Integer stock, Boolean isAvailable, Integer categoryId, String categoryName) {
        this.id = id; this.name = name; this.description = description; this.price = price;
        this.stock = stock; this.isAvailable = isAvailable;
        this.categoryId = categoryId; this.categoryName = categoryName;
    }

    // getters & setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
