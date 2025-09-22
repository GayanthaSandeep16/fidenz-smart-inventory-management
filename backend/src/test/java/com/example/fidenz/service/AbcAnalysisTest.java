package com.example.fidenz.service;

import com.example.fidenz.dto.AbcAnalysisResult;
import com.example.fidenz.entity.Product;
import com.example.fidenz.entity.SalesTransaction;
import com.example.fidenz.entity.Store;
import com.example.fidenz.repository.SalesTransactionRepository;
import com.example.fidenz.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbcAnalysisTest {

    @Mock
    private SalesTransactionRepository salesTransactionRepository;
    
    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private AbcAnalysisService abcAnalysisService;

    private Store testStore;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testStore = new Store();
        testStore.setId(1L);
        testStore.setName("Test Store");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setUnitPrice(new BigDecimal("100.00"));
        testProduct.setCategory("Electronics");
    }

    @Test
    void testPerformAbcAnalysis_WithValidData() {
        // Given
        Long storeId = 1L;
        int days = 30;
        List<SalesTransaction> transactions = createTestTransactions(5);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(testStore));
        when(salesTransactionRepository.findByStoreIdAndTransactionDateBetween(eq(storeId), any(), any()))
                .thenReturn(transactions);

        // When
        List<AbcAnalysisResult> results = abcAnalysisService.performAbcAnalysis(storeId, days);

        // Then
        assertNotNull(results);
        verify(storeRepository).findById(storeId);
        verify(salesTransactionRepository).findByStoreIdAndTransactionDateBetween(eq(storeId), any(), any());
    }

    @Test
    void testPerformAbcAnalysis_EmptyData() {
        // Given
        Long storeId = 1L;
        List<SalesTransaction> emptyTransactions = new ArrayList<>();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(testStore));
        when(salesTransactionRepository.findByStoreIdAndTransactionDateBetween(eq(storeId), any(), any()))
                .thenReturn(emptyTransactions);

        // When
        List<AbcAnalysisResult> results = abcAnalysisService.performAbcAnalysis(storeId, 30);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(storeRepository).findById(storeId);
        verify(salesTransactionRepository).findByStoreIdAndTransactionDateBetween(eq(storeId), any(), any());
    }

    @Test
    void testAbcAnalysisResultStructure() {
        // Given
        Long storeId = 1L;
        List<SalesTransaction> transactions = createTestTransactions(3);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(testStore));
        when(salesTransactionRepository.findByStoreIdAndTransactionDateBetween(eq(storeId), any(), any()))
                .thenReturn(transactions);

        // When
        List<AbcAnalysisResult> results = abcAnalysisService.performAbcAnalysis(storeId, 30);

        // Then
        assertNotNull(results);
        if (!results.isEmpty()) {
            AbcAnalysisResult result = results.get(0);
            assertNotNull(result.product());
            assertNotNull(result.totalRevenue());
            assertNotNull(result.category());
            assertTrue(result.totalRevenue().compareTo(BigDecimal.ZERO) >= 0);
            assertTrue(result.percentageOfTotal().compareTo(BigDecimal.ZERO) >= 0);
            assertTrue(result.cumulativePercentage().compareTo(BigDecimal.ZERO) >= 0);
        }
    }

    @Test
    void aggregateRevenueByProduct_shouldSumByProduct() {
        // Given
        List<SalesTransaction> transactions = createTestTransactions(3);
        transactions.get(0).setTotalAmount(new BigDecimal("10.00"));
        transactions.get(1).setTotalAmount(new BigDecimal("15.50"));
        transactions.get(2).setTotalAmount(new BigDecimal("4.50"));

        // When
        AbcAnalysisService svc = abcAnalysisService; // use injected instance
        var map = svc.aggregateRevenueByProduct(transactions);

        // Then
        assertNotNull(map);
        assertEquals(new BigDecimal("30.00"), map.get(testProduct));
    }

    @Test
    void calculateGrandTotal_shouldSumAllValues() {
        // Given
        AbcAnalysisService svc = abcAnalysisService;
        var map = new java.util.HashMap<Product, BigDecimal>();
        map.put(testProduct, new BigDecimal("12.34"));

        // When
        BigDecimal total = svc.calculateGrandTotal(map);

        // Then
        assertEquals(new BigDecimal("12.34"), total);
    }

    @Test
    void sortByRevenueDescending_shouldOrderByValue() {
        // Given
        AbcAnalysisService svc = abcAnalysisService;
        var p2 = new Product(); p2.setId(2L); p2.setName("P2");
        var map = new java.util.HashMap<Product, BigDecimal>();
        map.put(testProduct, new BigDecimal("5"));
        map.put(p2, new BigDecimal("10"));

        // When
        var sorted = svc.sortByRevenueDescending(map);

        // Then
        assertEquals(p2, sorted.get(0).getKey());
        assertEquals(testProduct, sorted.get(1).getKey());
    }

    @Test
    void percentage_handlesZeroDenominator() {
        AbcAnalysisService svc = abcAnalysisService;
        assertEquals(BigDecimal.ZERO, svc.percentage(new BigDecimal("10"), BigDecimal.ZERO));
    }

    @Test
    void determineCategory_thresholds() {
        AbcAnalysisService svc = abcAnalysisService;
        assertEquals("A", svc.determineCategory(AbcAnalysisService.CATEGORY_A_THRESHOLD));
        assertEquals("B", svc.determineCategory(new BigDecimal("90")));
        assertEquals("C", svc.determineCategory(AbcAnalysisService.CATEGORY_B_THRESHOLD.add(new BigDecimal("1"))));
    }

    @Test
    void groupByCategory_handlesNullAndEmpty() {
        AbcAnalysisService svc = abcAnalysisService;
        assertTrue(svc.groupByCategory(null).isEmpty());
        assertTrue(svc.groupByCategory(new ArrayList<>()).isEmpty());
    }

    @Test
    void groupByCategory_groupsCorrectly() {
        AbcAnalysisService svc = abcAnalysisService;
        var list = new ArrayList<AbcAnalysisResult>();
        list.add(AbcAnalysisResult.builder().product(testProduct).totalRevenue(BigDecimal.ONE).percentageOfTotal(BigDecimal.ONE).cumulativePercentage(BigDecimal.ONE).category("A").build());
        list.add(AbcAnalysisResult.builder().product(testProduct).totalRevenue(BigDecimal.ONE).percentageOfTotal(BigDecimal.ONE).cumulativePercentage(BigDecimal.ONE).category("B").build());
        list.add(AbcAnalysisResult.builder().product(testProduct).totalRevenue(BigDecimal.ONE).percentageOfTotal(BigDecimal.ONE).cumulativePercentage(BigDecimal.ONE).category("A").build());

        var grouped = svc.groupByCategory(list);
        assertEquals(2, grouped.get("A").size());
        assertEquals(1, grouped.get("B").size());
    }

    @Test
    void summarizeByCategory_handlesNullAndEmpty() {
        AbcAnalysisService svc = abcAnalysisService;
        var summaryNull = svc.summarizeByCategory(null);
        assertEquals(0L, summaryNull.get("A"));
        assertEquals(0L, summaryNull.get("B"));
        assertEquals(0L, summaryNull.get("C"));

        var summaryEmpty = svc.summarizeByCategory(new ArrayList<>());
        assertEquals(0L, summaryEmpty.get("A"));
        assertEquals(0L, summaryEmpty.get("B"));
        assertEquals(0L, summaryEmpty.get("C"));
    }

    @Test
    void summarizeByCategory_countsCorrectly() {
        AbcAnalysisService svc = abcAnalysisService;
        var list = new ArrayList<AbcAnalysisResult>();
        list.add(AbcAnalysisResult.builder().product(testProduct).totalRevenue(BigDecimal.ONE).percentageOfTotal(BigDecimal.ONE).cumulativePercentage(BigDecimal.ONE).category("A").build());
        list.add(AbcAnalysisResult.builder().product(testProduct).totalRevenue(BigDecimal.ONE).percentageOfTotal(BigDecimal.ONE).cumulativePercentage(BigDecimal.ONE).category("B").build());
        list.add(AbcAnalysisResult.builder().product(testProduct).totalRevenue(BigDecimal.ONE).percentageOfTotal(BigDecimal.ONE).cumulativePercentage(BigDecimal.ONE).category("A").build());

        var summary = svc.summarizeByCategory(list);
        assertEquals(2L, summary.get("A"));
        assertEquals(1L, summary.get("B"));
        assertEquals(0L, summary.get("C"));
    }

    // Helper method to create test data
    private List<SalesTransaction> createTestTransactions(int count) {
        List<SalesTransaction> transactions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            SalesTransaction transaction = new SalesTransaction();
            transaction.setId((long) i);
            transaction.setProduct(testProduct);
            transaction.setStore(testStore);
            transaction.setQuantity(2);
            transaction.setUnitPrice(testProduct.getUnitPrice());
            transaction.setTotalAmount(testProduct.getUnitPrice().multiply(new BigDecimal("2")));
            transaction.setTransactionDate(LocalDateTime.now().minusDays(i));
            transactions.add(transaction);
        }
        return transactions;
    }
}
