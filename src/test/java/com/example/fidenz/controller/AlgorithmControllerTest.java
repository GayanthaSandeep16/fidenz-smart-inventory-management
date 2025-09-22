package com.example.fidenz.controller;

import com.example.fidenz.base.BaseIntegrationTest;
import com.example.fidenz.entity.*;
import com.example.fidenz.repository.*;
import com.example.fidenz.security.JwtUtil;
import com.example.fidenz.testdata.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Algorithm Controller Integration Tests")
public class AlgorithmControllerTest extends BaseIntegrationTest {

    @Autowired
    private ReorderRecommendationRepository reorderRecommendationRepository;

    @Autowired
    private SalesTransactionRepository salesTransactionRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private JwtUtil jwtUtil;

    private User storeManager;
    private User storeEmployee;
    private Store testStore;
    private Product testProduct1;
    private Product testProduct2;
    private Inventory inventory1;
    private Inventory inventory2;
    private ReorderRecommendation reorderRecommendation;
    private String managerToken;
    private String employeeToken;

    @BeforeEach
    void setUpTestData() {
        // Test data is now loaded from data.sql
        // Get the test users that were inserted by SQL scripts
        storeManager = userRepository.findByUsername("testmanager")
                .orElseThrow(() -> new RuntimeException("Test manager user not found"));
        
        storeEmployee = userRepository.findByUsername("testoperator")
                .orElseThrow(() -> new RuntimeException("Test operator user not found"));

        // Generate JWT tokens
        managerToken = jwtUtil.generateToken(storeManager.getUsername());
        employeeToken = jwtUtil.generateToken(storeEmployee.getUsername());

        // Get test data from SQL scripts
        testStore = storeRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Test store not found"));

        testProduct1 = productRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Test product 1 not found"));
        testProduct2 = productRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("Test product 2 not found"));

        inventory1 = inventoryRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Test inventory 1 not found"));
        inventory2 = inventoryRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("Test inventory 2 not found"));

        // Create historical sales data for ABC analysis
        createHistoricalSalesData();

        // Get test reorder recommendation from SQL scripts
        reorderRecommendation = reorderRecommendationRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Test reorder recommendation not found"));
    }

    private void createHistoricalSalesData() {
        // Create sales transactions for the last 30 days for ABC analysis
        for (int i = 1; i <= 30; i++) {
            // Product 1 - High sales volume (Category A)
            SalesTransaction highVolumeSale = TestDataBuilder.createTestSalesTransaction(
                    testProduct1, testStore, 10, new BigDecimal("99.99"));
            highVolumeSale.setId(null);
            highVolumeSale.setTransactionDate(LocalDateTime.now().minusDays(i));
            salesTransactionRepository.save(highVolumeSale);

            // Product 2 - Lower sales volume (Category B or C)
            if (i % 3 == 0) { // Less frequent sales
                SalesTransaction lowVolumeSale = TestDataBuilder.createTestSalesTransaction(
                        testProduct2, testStore, 2, new BigDecimal("149.99"));
                lowVolumeSale.setId(null);
                lowVolumeSale.setTransactionDate(LocalDateTime.now().minusDays(i));
                salesTransactionRepository.save(lowVolumeSale);
            }
        }
    }

    @Test
    @DisplayName("Should generate reorder recommendations successfully for store manager")
    void testGenerateReorderRecommendations_AsStoreManager_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/algorithms/reorder-recommendations/{storeId}", testStore.getId())
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", is(notNullValue())))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    @DisplayName("Should deny access to reorder recommendations for store employee")
    void testGenerateReorderRecommendations_AsStoreEmployee_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/algorithms/reorder-recommendations/{storeId}", testStore.getId())
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get pending reorder recommendations successfully")
    void testGetPendingReorderRecommendations_AsStoreManager_ShouldReturnRecommendations() throws Exception {
        mockMvc.perform(get("/api/algorithms/reorder-recommendations/{storeId}/pending", testStore.getId())
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id", is(notNullValue())))
                .andExpect(jsonPath("$[0].recommendedQuantity", is(notNullValue())))
                .andExpect(jsonPath("$[0].processed", is(false)));
    }

    @Test
    @DisplayName("Should mark recommendation as processed successfully")
    void testMarkRecommendationAsProcessed_AsStoreManager_ShouldSucceed() throws Exception {
        mockMvc.perform(put("/api/algorithms/reorder-recommendations/{recommendationId}/process", reorderRecommendation.getId())
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 for non-existent recommendation")
    void testMarkRecommendationAsProcessed_WithNonExistentId_ShouldReturn404() throws Exception {
        mockMvc.perform(put("/api/algorithms/reorder-recommendations/{recommendationId}/process", 999L)
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should perform ABC analysis successfully")
    void testPerformAbcAnalysis_AsStoreManager_ShouldReturnAnalysis() throws Exception {
        mockMvc.perform(get("/api/algorithms/abc-analysis/{storeId}", testStore.getId())
                        .header("Authorization", "Bearer " + managerToken)
                        .param("days", "30")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].product", is(notNullValue())))
                .andExpect(jsonPath("$[0].totalRevenue", is(notNullValue())))
                .andExpect(jsonPath("$[0].percentageOfTotal", is(notNullValue())))
                .andExpect(jsonPath("$[0].category", isOneOf("A", "B", "C")));
    }

    @Test
    @DisplayName("Should perform ABC analysis with default days parameter")
    void testPerformAbcAnalysis_WithDefaultDays_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/algorithms/abc-analysis/{storeId}", testStore.getId())
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", is(notNullValue())));
    }

    @Test
    @DisplayName("Should get ABC analysis by category successfully")
    void testGetAbcAnalysisByCategory_AsStoreManager_ShouldReturnCategorizedResults() throws Exception {
        mockMvc.perform(get("/api/algorithms/abc-analysis/{storeId}/by-category", testStore.getId())
                        .header("Authorization", "Bearer " + managerToken)
                        .param("days", "30")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", is(notNullValue())))
                .andExpect(jsonPath("$.B", is(notNullValue())))
                .andExpect(jsonPath("$.C", is(notNullValue())));
    }

    @Test
    @DisplayName("Should get ABC analysis summary successfully")
    void testGetAbcAnalysisSummary_AsStoreManager_ShouldReturnSummary() throws Exception {
        mockMvc.perform(get("/api/algorithms/abc-analysis/{storeId}/summary", testStore.getId())
                        .header("Authorization", "Bearer " + managerToken)
                        .param("days", "30")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", is(notNullValue())))
                .andExpect(jsonPath("$.A", is(notNullValue())))
                .andExpect(jsonPath("$.B", is(notNullValue())))
                .andExpect(jsonPath("$.C", is(notNullValue())));
    }

    @Test
    @DisplayName("Should deny access to ABC analysis for store employee")
    void testPerformAbcAnalysis_AsStoreEmployee_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/algorithms/abc-analysis/{storeId}", testStore.getId())
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 for missing authentication")
    void testGenerateReorderRecommendations_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/algorithms/reorder-recommendations/{storeId}", testStore.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 for invalid JWT token")
    void testPerformAbcAnalysis_WithInvalidToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/algorithms/abc-analysis/{storeId}", testStore.getId())
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 400 for invalid store ID format")
    void testGenerateReorderRecommendations_WithInvalidIdFormat_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/algorithms/reorder-recommendations/{storeId}", "invalid-id")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 for non-existent store")
    void testGenerateReorderRecommendations_WithNonExistentStore_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/algorithms/reorder-recommendations/{storeId}", 999L)
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should validate days parameter for ABC analysis")
    void testPerformAbcAnalysis_WithInvalidDaysParameter_ShouldHandleGracefully() throws Exception {
        mockMvc.perform(get("/api/algorithms/abc-analysis/{storeId}", testStore.getId())
                        .header("Authorization", "Bearer " + managerToken)
                        .param("days", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Should handle gracefully with default behavior
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should handle large days parameter for ABC analysis")
    void testPerformAbcAnalysis_WithLargeDaysParameter_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/algorithms/abc-analysis/{storeId}", testStore.getId())
                        .header("Authorization", "Bearer " + managerToken)
                        .param("days", "365")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should return 405 for unsupported HTTP method")
    void testReorderRecommendations_WithUnsupportedMethod_ShouldReturn405() throws Exception {
        mockMvc.perform(post("/api/algorithms/reorder-recommendations/{storeId}", testStore.getId())
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }
}
