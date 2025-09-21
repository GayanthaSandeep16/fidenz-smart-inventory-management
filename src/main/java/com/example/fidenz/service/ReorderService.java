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

        List<Inventory> inventories = inventoryRepository.findByStoreIdWithDetails(storeId);
        List<ReorderRecommendation> recommendations = new ArrayList<>();

        for (Inventory inventory : inventories) {
            try {
                log.debug("Processing inventory for product: {} (ID: {}), current stock: {}", 
                         inventory.getProduct().getName(), inventory.getProduct().getId(), inventory.getCurrentStock());
                
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
            // create a basic recommendation based on minimum stock requirements
            Integer currentStock = inventory.getCurrentStock();
            Integer minStock = product.getMinStorageQty();
            
            if (currentStock == 0 || (minStock != null && currentStock <= minStock)) {
                log.info("Creating basic reorder recommendation for low-stock product {} in store {} (currentStock: {}, minStock: {})", 
                         product.getName(), store.getId(), currentStock, minStock);
                return createBasicReorderRecommendation(inventory);
            }
            
            return null;
        }

        // Calculate Average Daily Sales
        BigDecimal totalQuantity = sales.stream()
                .map(transaction -> BigDecimal.valueOf(transaction.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageDailySales = totalQuantity.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);

        // Calculate Seasonality Factor based on weekday vs weekend sales patterns
        // SeasonalityFactor = (WeekdayCount × 0.8 + WeekendCount × 1.4) / 7
        long weekdayCount = sales.stream()
                .mapToLong(transaction -> {
                    DayOfWeek dayOfWeek = transaction.getTransactionDate().getDayOfWeek();
                    return (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) ? 0 : 1;
                })
                .sum();
        
        long weekendCount = sales.size() - weekdayCount;
        
        // Apply the seasonality formula: (WeekdayCount × 0.8 + WeekendCount × 1.4) / 7
        BigDecimal weekdayFactor = BigDecimal.valueOf(weekdayCount).multiply(BigDecimal.valueOf(0.8));
        BigDecimal weekendFactor = BigDecimal.valueOf(weekendCount).multiply(BigDecimal.valueOf(1.4));
        BigDecimal seasonalityFactor = weekdayFactor.add(weekendFactor).divide(BigDecimal.valueOf(7), 2, RoundingMode.HALF_UP);
        
        // Ensure seasonality factor is at least 0.1 to avoid zero calculations
        if (seasonalityFactor.compareTo(BigDecimal.valueOf(0.1)) < 0) {
            seasonalityFactor = BigDecimal.valueOf(0.1);
        }
        
        log.debug("Seasonality calculation for {}: weekdayCount={}, weekendCount={}, seasonalityFactor={}", 
                 product.getName(), weekdayCount, weekendCount, seasonalityFactor);

        // Calculate Adjusted Sales
        BigDecimal adjustedSales = averageDailySales.multiply(seasonalityFactor);

        // Calculate Safety Stock (assuming 2 days of safety stock)
        Integer safetyStock = adjustedSales.multiply(BigDecimal.valueOf(2)).intValue();

        // Calculate Reorder Point
        Integer reorderPoint = adjustedSales.multiply(BigDecimal.valueOf(7)).intValue() + safetyStock; // 7 days lead time

        // Calculate Recommended Quantity
        Integer currentStock = inventory.getCurrentStock();
        Integer reorderQty = reorderPoint - currentStock;

        log.debug("Reorder calculation for {}: avgDailySales={}, seasonalityFactor={}, adjustedSales={}, safetyStock={}, reorderPoint={}, currentStock={}, reorderQty={}", 
                 product.getName(), averageDailySales, seasonalityFactor, adjustedSales, safetyStock, reorderPoint, currentStock, reorderQty);

        if (reorderQty <= 0) {
            log.debug("Standard reorder calculation shows no need for product {} in store {}. Current stock: {}, Reorder point: {}", 
                     product.getName(), store.getId(), currentStock, reorderPoint);
            
            // Check if this is a low-stock situation that still needs attention
            Integer minStock = product.getMinStorageQty();
            if (currentStock <= 5 || (minStock != null && currentStock <= minStock)) {
                log.info("Low stock detected for product {} in store {} (currentStock: {}, minStock: {}). Creating basic recommendation.", 
                         product.getName(), store.getId(), currentStock, minStock);
                return createBasicReorderRecommendation(inventory);
            }
            
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

    /**
     * Create a basic reorder recommendation for items with no recent sales data
     * but are critically low on stock (out of stock or below minimum threshold).
     */
    private ReorderRecommendation createBasicReorderRecommendation(Inventory inventory) {
        Product product = inventory.getProduct();
        Store store = inventory.getStore();
        Integer currentStock = inventory.getCurrentStock();
        
        // Use minimum stock as baseline, or default to 10 if not set
        Integer minStock = product.getMinStorageQty() != null ? product.getMinStorageQty() : 10;
        
        // Basic recommendation: order enough to reach 50% of max storage or 3x min stock, whichever is smaller
        Integer maxStock = product.getMaxStorageQty() != null ? product.getMaxStorageQty() : 100;
        Integer targetStock = Math.min(maxStock / 2, minStock * 3);
        Integer reorderQty = Math.max(targetStock - currentStock, minStock);
        
        // Round up to nearest 5 for basic recommendations
        reorderQty = ((reorderQty + 4) / 5) * 5;
        
        // Ensure we don't exceed max storage
        if (product.getMaxStorageQty() != null) {
            int maxAdditionalStock = product.getMaxStorageQty() - currentStock;
            reorderQty = Math.min(reorderQty, maxAdditionalStock);
        }
        
        if (reorderQty <= 0) {
            return null;
        }
        
        // Create basic recommendation with default values
        ReorderRecommendation recommendation = new ReorderRecommendation();
        recommendation.setProduct(product);
        recommendation.setStore(store);
        recommendation.setCurrentStock(currentStock);
        recommendation.setAverageDailySales(BigDecimal.ONE);
        recommendation.setSeasonalityFactor(BigDecimal.ONE);
        recommendation.setAdjustedSales(BigDecimal.ONE);
        recommendation.setSafetyStock(minStock); // Use min stock as safety stock
        recommendation.setReorderPoint(minStock * 2); // Simple reorder point
        recommendation.setRecommendedQuantity(reorderQty);
        recommendation.setProcessed(false);
        
        log.info("Created basic reorder recommendation for {} in store {}: {} units", 
                 product.getName(), store.getId(), reorderQty);
        
        return recommendation;
    }

    public List<ReorderRecommendation> getReorderRecommendations(Long storeId) {
        return reorderRecommendationRepository.findByStoreIdAndProcessedWithDetails(storeId, false);
    }

    @Transactional
    public void markRecommendationAsProcessed(Long recommendationId) {
        ReorderRecommendation recommendation = reorderRecommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new EntityNotFoundException("Recommendation not found"));
        
        recommendation.setProcessed(true);
        reorderRecommendationRepository.save(recommendation);
    }
}
