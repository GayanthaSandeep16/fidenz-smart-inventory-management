package com.example.fidenz.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesTransactionRequest {
    
    @NotNull
    private Long productId;
    
    @NotNull
    private Long storeId;
    
    @NotNull
    @Positive
    private Integer quantity;
    
    @NotNull
    private BigDecimal unitPrice;
}
