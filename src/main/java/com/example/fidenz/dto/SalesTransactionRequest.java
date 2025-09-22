package com.example.fidenz.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SalesTransactionRequest(
    @NotNull
    Long productId,
    
    @NotNull
    Long storeId,
    
    @NotNull
    @Positive
    Integer quantity,
    
    @NotNull
    BigDecimal unitPrice
) {}
