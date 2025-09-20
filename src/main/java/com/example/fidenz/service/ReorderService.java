package com.example.fidenz.service;

import com.example.fidenz.entity.*;
import com.example.fidenz.exception.EntityNotFoundException;
import com.example.fidenz.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReorderService {

    private static final Logger log = LoggerFactory.getLogger(ReorderService.class);

    private final InventoryRepository inventoryRepository;
    private final SalesTransactionRepository salesTransactionRepository;
    private final ReorderRecommendationRepository reorderRecommendationRepository;
    private final StoreRepository storeRepository;

    public ReorderService(InventoryRepository inventoryRepository, SalesTransactionRepository salesTransactionRepository,
                         ReorderRecommendationRepository reorderRecommendationRepository, StoreRepository storeRepository) {
        this.inventoryRepository = inventoryRepository;
        this.salesTransactionRepository = salesTransactionRepository;
        this.reorderRecommendationRepository = reorderRecommendationRepository;
        this.storeRepository = storeRepository;
    }

    /**
     * Generate reorder suggestions for all products in a given store.
     *
     * @param storeId The ID of the store.
     * @return A list of ReorderRecommendation.
     */
    @Transactional
    public List<ReorderRecommendation> generateReorderSuggestions(Long storeId) {
        log.info("Generating reorder suggestions for store: {}", storeId);
        
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store", storeId));

        List<Inventory> inventories = inventoryRepository.findByStore(store);
        List<ReorderRecommendation> recommendations = new ArrayList<>();

        for (Inventory inventory : inventories) {
            try {
                ReorderRecommendation recommendation = calculateReorderRecommendation(inventory);
                if (recommendation != null) {
                    // Check if recommendation already exists
                    Optional<ReorderRecommendation> existing = reorderRecommendationRepository
                            .findByProductAndStore(inventory.getProduct(), store);
                    
                    if (existing.isPresent()) {
                        // Update existing recommendation
                        ReorderRecommendation existingRec = existing.get();
                        existingRec.setCurrentStock(recommendation.getCurrentStock());
                        existingRec.setAverageDailySales(recommendation.getAverageDailySales());
                        existingRec.setSeasonalityFactor(recommendation.getSeasonalityFactor());
                        existingRec.setAdjustedSales(recommendation.getAdjustedSales());
                        existingRec.setSafetyStock(recommendation.getSafetyStock());
                        existingRec.setReorderPoint(recommendation.getReorderPoint());
                        existingRec.setRecommendedQuantity(recommendation.getRecommendedQuantity());
                        existingRec.setProcessed(false);
                        reorderRecommendationRepository.save(existingRec);
                        recommendations.add(existingRec);
                    } else {
                        // Create new recommendation
                        reorderRecommendationRepository.save(recommendation);
                        recommendations.add(recommendation);
                    }
                }
            } catch (Exception e) {
                log.error("Error calculating reorder recommendation for product {} in store {}: {}", 
                         inventory.getProduct().getName(), storeId, e.getMessage());
            }
        }

        log.info("Generated {} reorder recommendations for store: {}", recommendations.size(), storeId);
        return recommendations;
    }

    private ReorderRecommendation calculateReorderRecommendation(Inventory inventory) {
        Product product = inventory.getProduct();
        Store store = inventory.getStore();
        
        // Get sales data for the last 30 days
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);
        
        List<SalesTransaction> sales = salesTransactionRepository
                .findByStoreIdAndTransactionDateBetween(store.getId(), startDate, endDate)
                .stream()
                .filter(transaction -> transaction.getProduct().getId().equals(product.getId()))
                .collect(Collectors.toList());

        if (sales.isEmpty()) {
            log.debug("No sales data found for product {} in store {} for the last 30 days", 
                     product.getName(), store.getId());
            return null;
        }

        // Calculate Average Daily Sales
        BigDecimal totalQuantity = sales.stream()
                .map(transaction -> BigDecimal.valueOf(transaction.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageDailySales = totalQuantity.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);

        // Calculate Seasonality Factor
        long weekdayCount = sales.stream()
                .mapToLong(transaction -> {
                    DayOfWeek dayOfWeek = transaction.getTransactionDate().getDayOfWeek();
                    return (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) ? 0 : 1;
                })
                .sum();
        
        long weekendCount = 30 - weekdayCount;
        BigDecimal seasonalityFactor = BigDecimal.valueOf(weekdayCount)
                .divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);

        // Calculate Adjusted Sales
        BigDecimal adjustedSales = averageDailySales.multiply(seasonalityFactor);

        // Calculate Safety Stock (assuming 2 days of safety stock)
        Integer safetyStock = adjustedSales.multiply(BigDecimal.valueOf(2)).intValue();

        // Calculate Reorder Point
        Integer reorderPoint = adjustedSales.multiply(BigDecimal.valueOf(7)).intValue() + safetyStock; // 7 days lead time

        // Calculate Recommended Quantity
        Integer currentStock = inventory.getCurrentStock();
        Integer reorderQty = reorderPoint - currentStock;

        if (reorderQty <= 0) {
            log.debug("No reorder needed for product {} in store {}. Current stock: {}, Reorder point: {}", 
                     product.getName(), store.getId(), currentStock, reorderPoint);
            return null;
        }

        // Apply business rules: round up to nearest 10
        reorderQty = ((reorderQty + 9) / 10) * 10;

        // Check against Max Storage Quantity
        if (product.getMaxStorageQty() != null) {
            int maxAdditionalStock = product.getMaxStorageQty() - currentStock;
            reorderQty = Math.min(reorderQty, maxAdditionalStock);
        }

        if (reorderQty <= 0) {
            log.debug("Reorder quantity would exceed max storage for product {} in store {}", 
                     product.getName(), store.getId());
            return null;
        }

        // Create recommendation
        ReorderRecommendation recommendation = new ReorderRecommendation();
        recommendation.setProduct(product);
        recommendation.setStore(store);
        recommendation.setCurrentStock(currentStock);
        recommendation.setAverageDailySales(averageDailySales);
        recommendation.setSeasonalityFactor(seasonalityFactor);
        recommendation.setAdjustedSales(adjustedSales);
        recommendation.setSafetyStock(safetyStock);
        recommendation.setReorderPoint(reorderPoint);
        recommendation.setRecommendedQuantity(reorderQty);
        recommendation.setProcessed(false);

        return recommendation;
    }

    public List<ReorderRecommendation> getReorderRecommendations(Long storeId) {
        return reorderRecommendationRepository.findByStoreIdAndProcessed(storeId, false);
    }

    @Transactional
    public void markRecommendationAsProcessed(Long recommendationId) {
        ReorderRecommendation recommendation = reorderRecommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new EntityNotFoundException("Recommendation not found"));
        
        recommendation.setProcessed(true);
        reorderRecommendationRepository.save(recommendation);
    }
}
