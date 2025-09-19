package com.example.fidenz.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reorder_recommendations")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ReorderRecommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull
    @JsonManagedReference
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    @NotNull
    @JsonManagedReference
    private Store store;
    
    @Column(name = "current_stock", nullable = false)
    @NotNull
    private Integer currentStock;
    
    @Column(name = "average_daily_sales", precision = 10, scale = 2)
    private BigDecimal averageDailySales;
    
    @Column(name = "seasonality_factor", precision = 5, scale = 2)
    private BigDecimal seasonalityFactor;
    
    @Column(name = "adjusted_sales", precision = 10, scale = 2)
    private BigDecimal adjustedSales;
    
    @Column(name = "safety_stock", nullable = false)
    @NotNull
    private Integer safetyStock;
    
    @Column(name = "reorder_point", nullable = false)
    @NotNull
    private Integer reorderPoint;
    
    @Column(name = "recommended_qty", nullable = false)
    @NotNull
    private Integer recommendedQty;
    
    @Column(name = "is_processed", nullable = false)
    private Boolean isProcessed = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
