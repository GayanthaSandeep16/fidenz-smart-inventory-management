package com.example.fidenz.service;

import com.example.fidenz.dto.AbcAnalysisResult;
import com.example.fidenz.entity.Product;
import com.example.fidenz.entity.SalesTransaction;
import com.example.fidenz.entity.Store;
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
                .orElseThrow(() -> new RuntimeException("Store not found"));

        // Get sales data for the specified period
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        List<SalesTransaction> sales = salesTransactionRepository
                .findByStoreIdAndTransactionDateBetween(storeId, startDate, endDate);

        if (sales.isEmpty()) {
            log.warn("No sales data found for store {} in the last {} days", storeId, days);
            return Collections.emptyList();
        }

        // Calculate total revenue for each product
        Map<Product, BigDecimal> productRevenue = sales.stream()
                .collect(Collectors.groupingBy(
                        SalesTransaction::getProduct,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                transaction -> transaction.getTotalAmount(),
                                BigDecimal::add
                        )
                ));

        // Calculate grand total revenue
        BigDecimal grandTotal = productRevenue.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (grandTotal.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("No revenue found for store {} in the last {} days", storeId, days);
            return Collections.emptyList();
        }

        // Sort products by revenue in descending order
        List<Map.Entry<Product, BigDecimal>> sortedProducts = productRevenue.entrySet().stream()
                .sorted(Map.Entry.<Product, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toList());

        // Calculate ABC categories
        List<AbcAnalysisResult> results = new ArrayList<>();
        BigDecimal cumulativeRevenue = BigDecimal.ZERO;

        for (Map.Entry<Product, BigDecimal> entry : sortedProducts) {
            Product product = entry.getKey();
            BigDecimal revenue = entry.getValue();
            
            // Calculate percentage of total revenue
            BigDecimal percentageOfTotal = revenue
                    .divide(grandTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            // Calculate cumulative percentage
            cumulativeRevenue = cumulativeRevenue.add(revenue);
            BigDecimal cumulativePercentage = cumulativeRevenue
                    .divide(grandTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            // Determine category
            String category = determineCategory(cumulativePercentage);

            AbcAnalysisResult result = new AbcAnalysisResult();
            result.setProduct(product);
            result.setTotalRevenue(revenue);
            result.setPercentageOfTotal(percentageOfTotal);
            result.setCumulativePercentage(cumulativePercentage);
            result.setCategory(category);

            results.add(result);
        }

        log.info("ABC analysis completed for store: {}. Found {} products", storeId, results.size());
        return results;
    }

    /**
     * Determine the ABC category based on cumulative percentage.
     * @param cumulativePercentage
     * @return
     */
    private String determineCategory(BigDecimal cumulativePercentage) {
        if (cumulativePercentage.compareTo(BigDecimal.valueOf(80)) <= 0) {
            return "A";
        } else if (cumulativePercentage.compareTo(BigDecimal.valueOf(95)) <= 0) {
            return "B";
        } else {
            return "C";
        }
    }

    public Map<String, List<AbcAnalysisResult>> getAbcAnalysisByCategory(Long storeId, int days) {
        List<AbcAnalysisResult> results = performAbcAnalysis(storeId, days);
        
        return results.stream()
                .collect(Collectors.groupingBy(AbcAnalysisResult::getCategory));
    }

    public Map<String, Long> getAbcAnalysisSummary(Long storeId, int days) {
        List<AbcAnalysisResult> results = performAbcAnalysis(storeId, days);
        
        Map<String, Long> summary = new HashMap<>();
        summary.put("A", results.stream().filter(r -> "A".equals(r.getCategory())).count());
        summary.put("B", results.stream().filter(r -> "B".equals(r.getCategory())).count());
        summary.put("C", results.stream().filter(r -> "C".equals(r.getCategory())).count());
        
        return summary;
    }
}
