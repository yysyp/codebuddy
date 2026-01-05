package com.demo.ignite.entity;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;

/**
 * Product Entity for Ignite Cache
 * This class represents a product that will be stored in the in-memory SQL cache
 */
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @QuerySqlField(index = true)
    private Long id;

    @QuerySqlField
    private String name;

    @QuerySqlField
    private String description;

    @QuerySqlField
    private Double price;

    @QuerySqlField
    private String category;

    @QuerySqlField
    private Integer stock;

    @QuerySqlField(index = true)
    private Boolean available;

    @QuerySqlField
    private Long createdAt;

    /**
     * Default constructor required by Ignite
     */
    public Product() {
    }

    /**
     * Constructor with all fields
     */
    public Product(Long id, String name, String description, Double price, String category, Integer stock, Boolean available, Long createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.available = available;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", stock=" + stock +
                ", available=" + available +
                ", createdAt=" + createdAt +
                '}';
    }
}
