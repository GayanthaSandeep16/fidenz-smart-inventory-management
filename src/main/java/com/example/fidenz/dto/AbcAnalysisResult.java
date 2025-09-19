package com.example.fidenz.dto;

import com.example.fidenz.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbcAnalysisResult {

    private Product product;
    private BigDecimal totalRevenue;
    private BigDecimal percentageOfTotal;
    private BigDecimal cumulativePercentage;
    private String category; // A, B, or C


}
