package com.example.fidenz.dto;

import java.time.LocalDateTime;

/**
 * InventoryResponse DTO to encapsulate inventory details along with associated product and store information.
 */
public record InventoryResponse(
    Long id,
    Integer currentStock,
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
