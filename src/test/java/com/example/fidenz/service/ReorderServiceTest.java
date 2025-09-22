package com.example.fidenz.service;

import com.example.fidenz.entity.*;
import com.example.fidenz.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReorderServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;
    
    @Mock
    private SalesTransactionRepository salesTransactionRepository;
    
    @Mock
    private ReorderRecommendationRepository reorderRecommendationRepository;
    
    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private ReorderService reorderService;

    private Store testStore;
    private Product testProduct;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        testStore = new Store();
        testStore.setId(1L);
        testStore.setName("Test Store");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setMaxStorageQty(100);
        testProduct.setMinStorageQty(10);
        testProduct.setUnitPrice(new BigDecimal("5.00"));

        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setStore(testStore);
        testInventory.setProduct(testProduct);
        testInventory.setCurrentStock(15);
    }

    @Test
    void testGenerateReorderSuggestions_StoreExists() {
        // Given
        Long storeId = 1L;
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(testStore));
        when(inventoryRepository.findByStoreIdWithDetails(storeId)).thenReturn(Arrays.asList(testInventory));
        when(salesTransactionRepository.findByStoreIdAndTransactionDateBetween(eq(storeId), any(), any()))
                .thenReturn(Arrays.asList());

        // When
        List<ReorderRecommendation> result = reorderService.generateReorderSuggestions(storeId);

        // Then
        assertNotNull(result);
        verify(storeRepository).findById(storeId);
        verify(inventoryRepository).findByStoreIdWithDetails(storeId);
    }

    @Test
    void testLowStockDetection_ZeroStock() {
        // Given
        testInventory.setCurrentStock(0);
        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
        when(inventoryRepository.findByStoreIdWithDetails(1L)).thenReturn(Arrays.asList(testInventory));
        when(salesTransactionRepository.findByStoreIdAndTransactionDateBetween(eq(1L), any(), any()))
                .thenReturn(Arrays.asList());
        when(reorderRecommendationRepository.findByProductAndStore(any(), any()))
                .thenReturn(Optional.empty());

        // When
        List<ReorderRecommendation> result = reorderService.generateReorderSuggestions(1L);

        // Then
        assertNotNull(result);
        verify(reorderRecommendationRepository).save(any(ReorderRecommendation.class));
    }

    @Test
    void testLowStockDetection_BelowMinimum() {
        // Given
        testInventory.setCurrentStock(5); // Below minStorageQty of 10
        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
        when(inventoryRepository.findByStoreIdWithDetails(1L)).thenReturn(Arrays.asList(testInventory));
        when(salesTransactionRepository.findByStoreIdAndTransactionDateBetween(eq(1L), any(), any()))
                .thenReturn(Arrays.asList());
        when(reorderRecommendationRepository.findByProductAndStore(any(), any()))
                .thenReturn(Optional.empty());

        // When
        List<ReorderRecommendation> result = reorderService.generateReorderSuggestions(1L);

        // Then
        assertNotNull(result);
        verify(reorderRecommendationRepository).save(any(ReorderRecommendation.class));
    }

    @Test
    void testSeasonalityFactorCalculation_WithSales() {
        // Given
        SalesTransaction weekdayTransaction = new SalesTransaction();
        weekdayTransaction.setProduct(testProduct);
        weekdayTransaction.setStore(testStore);
        weekdayTransaction.setQuantity(2);
        weekdayTransaction.setTransactionDate(LocalDateTime.now().minusDays(1));

        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
        when(inventoryRepository.findByStoreIdWithDetails(1L)).thenReturn(Arrays.asList(testInventory));
        when(salesTransactionRepository.findByStoreIdAndTransactionDateBetween(eq(1L), any(), any()))
                .thenReturn(Arrays.asList(weekdayTransaction));

        // When
        List<ReorderRecommendation> result = reorderService.generateReorderSuggestions(1L);

        // Then
        assertNotNull(result);
    }

    @Test
    void testGetReorderRecommendations() {
        // Given
        Long storeId = 1L;
        ReorderRecommendation mockRecommendation = new ReorderRecommendation();
        mockRecommendation.setId(1L);
        mockRecommendation.setProduct(testProduct);
        mockRecommendation.setStore(testStore);
        mockRecommendation.setCurrentStock(5);
        mockRecommendation.setRecommendedQuantity(20);
        mockRecommendation.setProcessed(false);

        when(reorderRecommendationRepository.findByStoreIdAndProcessedWithDetails(storeId, false))
                .thenReturn(Arrays.asList(mockRecommendation));

        // When
        List<ReorderRecommendation> result = reorderService.getReorderRecommendations(storeId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockRecommendation, result.get(0));
        verify(reorderRecommendationRepository).findByStoreIdAndProcessedWithDetails(storeId, false);
    }

    @Test
    void testMarkRecommendationAsProcessed() {
        // Given
        Long recommendationId = 1L;
        ReorderRecommendation mockRecommendation = new ReorderRecommendation();
        mockRecommendation.setId(recommendationId);
        mockRecommendation.setProcessed(false);

        when(reorderRecommendationRepository.findById(recommendationId))
                .thenReturn(Optional.of(mockRecommendation));

        // When
        reorderService.markRecommendationAsProcessed(recommendationId);

        // Then
        assertTrue(mockRecommendation.getProcessed());
        verify(reorderRecommendationRepository).save(mockRecommendation);
    }
}
