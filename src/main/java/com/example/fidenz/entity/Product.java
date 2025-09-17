package com.example.fidenz.entity;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String name;
    
    @Size(max = 500)
    private String description;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Size(max = 50)
    private String category;
    
    @Size(max = 20)
    private String sku;
    
    @Column(name = "max_storage_qty")
    private Integer maxStorageQty;
    
    @Column(name = "min_storage_qty")
    private Integer minStorageQty;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inventory> inventories;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SalesTransaction> salesTransactions;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReorderRecommendation> reorderRecommendations;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Product() {}

    public Product(String name, String description, BigDecimal unitPrice, String category, String sku, Integer maxStorageQty, Integer minStorageQty) {
        this.name = name;
        this.description = description;
        this.unitPrice = unitPrice;
        this.category = category;
        this.sku = sku;
        this.maxStorageQty = maxStorageQty;
        this.minStorageQty = minStorageQty;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public Integer getMaxStorageQty() { return maxStorageQty; }
    public void setMaxStorageQty(Integer maxStorageQty) { this.maxStorageQty = maxStorageQty; }

    public Integer getMinStorageQty() { return minStorageQty; }
    public void setMinStorageQty(Integer minStorageQty) { this.minStorageQty = minStorageQty; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Inventory> getInventories() { return inventories; }
    public void setInventories(List<Inventory> inventories) { this.inventories = inventories; }

    public List<SalesTransaction> getSalesTransactions() { return salesTransactions; }
    public void setSalesTransactions(List<SalesTransaction> salesTransactions) { this.salesTransactions = salesTransactions; }

    public List<ReorderRecommendation> getReorderRecommendations() { return reorderRecommendations; }
    public void setReorderRecommendations(List<ReorderRecommendation> reorderRecommendations) { this.reorderRecommendations = reorderRecommendations; }
}
