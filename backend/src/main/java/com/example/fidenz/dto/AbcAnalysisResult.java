package com.example.fidenz.dto;

import com.example.fidenz.entity.Product;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AbcAnalysisResult(
    Product product,
    BigDecimal totalRevenue,
    BigDecimal percentageOfTotal,
    BigDecimal cumulativePercentage,
    String category // A, B, or C
) {}
