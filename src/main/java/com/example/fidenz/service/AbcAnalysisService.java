package com.example.fidenz.service;

import com.example.fidenz.dto.AbcAnalysisResult;
import com.example.fidenz.entity.Product;
import com.example.fidenz.entity.SalesTransaction;
import com.example.fidenz.entity.Store;
import com.example.fidenz.exception.EntityNotFoundException;
import com.example.fidenz.repository.SalesTransactionRepository;
import com.example.fidenz.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AbcAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AbcAnalysisService.class);

    static final int PERCENT_SCALE = 4;
    static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    static final BigDecimal CATEGORY_A_THRESHOLD = BigDecimal.valueOf(80);
    static final BigDecimal CATEGORY_B_THRESHOLD = BigDecimal.valueOf(95);

    private final SalesTransactionRepository salesTransactionRepository;
    private final StoreRepository storeRepository;

    public AbcAnalysisService(SalesTransactionRepository salesTransactionRepository, StoreRepository storeRepository) {
        this.salesTransactionRepository = salesTransactionRepository;
        this.storeRepository = storeRepository;
    }

    /**
     * Perform ABC analysis for a given store over a specified number of days.
     *
     * @param storeId The ID of the store.
     * @param days    The number of days to look back for sales data.
     * @return A list of AbcAnalysisResult containing products categorized into A, B, and C.
     */
    public List<AbcAnalysisResult> performAbcAnalysis(Long storeId, int days) {
        log.info("Performing ABC analysis for store: {} for the last {} days", storeId, days);
        
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store not found"));

        // Get sales data for the specified period
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        List<SalesTransaction> sales = salesTransactionRepository
                .findByStoreIdAndTransactionDateBetween(storeId, startDate, endDate);

        if (sales.isEmpty()) {
            log.warn("No sales data found for store {} in the last {} days", storeId, days);
            return Collections.emptyList();
        }

        Map<Product, BigDecimal> productRevenue = aggregateRevenueByProduct(sales);

        BigDecimal grandTotal = calculateGrandTotal(productRevenue);

        if (grandTotal.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("No revenue found for store {} in the last {} days", storeId, days);
            return Collections.emptyList();
        }

        List<Map.Entry<Product, BigDecimal>> sortedProducts = sortByRevenueDescending(productRevenue);

        List<AbcAnalysisResult> results = buildResults(sortedProducts, grandTotal);

        log.info("ABC analysis completed for store: {}. Found {} products", storeId, results.size());
        return results;
    }

    /**
     * Determine the ABC category based on cumulative percentage.
     * @param cumulativePercentage
     * @return
     */
    String determineCategory(BigDecimal cumulativePercentage) {
        if (cumulativePercentage.compareTo(CATEGORY_A_THRESHOLD) <= 0) {
            return "A";
        } else if (cumulativePercentage.compareTo(CATEGORY_B_THRESHOLD) <= 0) {
            return "B";
        } else {
            return "C";
        }
    }

    // Aggregate total revenue per product using a simple loop
    Map<Product, BigDecimal> aggregateRevenueByProduct(List<SalesTransaction> sales) {
        Map<Product, BigDecimal> revenueByProduct = new HashMap<>();
        for (SalesTransaction transaction : sales) {
            Product product = transaction.getProduct();
            BigDecimal amount = transaction.getTotalAmount();
            if (amount == null) {
                amount = BigDecimal.ZERO;
            }
            revenueByProduct.merge(product, amount, BigDecimal::add);
        }
        return revenueByProduct;
    }

    // Calculate grand total revenue across all products
    BigDecimal calculateGrandTotal(Map<Product, BigDecimal> productRevenue) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal value : productRevenue.values()) {
            if (value != null) {
                total = total.add(value);
            }
        }
        return total;
    }

    // Sort entries by revenue descending
    List<Map.Entry<Product, BigDecimal>> sortByRevenueDescending(Map<Product, BigDecimal> productRevenue) {
        List<Map.Entry<Product, BigDecimal>> entries = new ArrayList<>(productRevenue.entrySet());
        entries.sort(Map.Entry.<Product, BigDecimal>comparingByValue(Comparator.nullsFirst(Comparator.naturalOrder())).reversed());
        return entries;
    }

    // Build analysis results with cumulative and percentage calculations
    List<AbcAnalysisResult> buildResults(List<Map.Entry<Product, BigDecimal>> sortedProducts, BigDecimal grandTotal) {
        List<AbcAnalysisResult> results = new ArrayList<>();
        BigDecimal cumulativeRevenue = BigDecimal.ZERO;

        for (Map.Entry<Product, BigDecimal> entry : sortedProducts) {
            Product product = entry.getKey();
            BigDecimal revenue = entry.getValue();
            if (revenue == null) {
                revenue = BigDecimal.ZERO;
            }

            BigDecimal percentageOfTotal = percentage(revenue, grandTotal);
            cumulativeRevenue = cumulativeRevenue.add(revenue);
            BigDecimal cumulativePercentage = percentage(cumulativeRevenue, grandTotal);

            String category = determineCategory(cumulativePercentage);

            AbcAnalysisResult result = new AbcAnalysisResult(
                    product,
                    revenue,
                    percentageOfTotal,
                    cumulativePercentage,
                    category
            );
            results.add(result);
        }
        return results;
    }

    // Helper for percentage computation with fixed scale/rounding
    BigDecimal percentage(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return numerator
                .divide(denominator, PERCENT_SCALE, ROUNDING_MODE)
                .multiply(ONE_HUNDRED);
    }

    public Map<String, List<AbcAnalysisResult>> getAbcAnalysisByCategory(Long storeId, int days) {
        List<AbcAnalysisResult> results = performAbcAnalysis(storeId, days);
        
        return results.stream()
                .collect(Collectors.groupingBy(AbcAnalysisResult::category));
    }

    public Map<String, Long> getAbcAnalysisSummary(Long storeId, int days) {
        List<AbcAnalysisResult> results = performAbcAnalysis(storeId, days);
        
        Map<String, Long> summary = new HashMap<>();
        summary.put("A", results.stream().filter(r -> "A".equals(r.category())).count());
        summary.put("B", results.stream().filter(r -> "B".equals(r.category())).count());
        summary.put("C", results.stream().filter(r -> "C".equals(r.category())).count());
        
        return summary;
    }
}
