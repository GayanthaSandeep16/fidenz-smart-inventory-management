package com.example.fidenz.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for ReorderRecommendation responses - includes product and store details without circular references
 */
public record ReorderRecommendationResponse(
    Long id,
    Integer currentStock,
    BigDecimal averageDailySales,
    BigDecimal seasonalityFactor,
    Integer leadTime,
    Integer safetyStock,
    Integer reorderPoint,
    Integer recommendedQuantity,
    Boolean processed,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    
    // Product details
    Long productId,
    String productName,
    String productCategory,
    String productSku,
    
    // Store details  
    Long storeId,
    String storeName,
    String storeLocation
) {}
