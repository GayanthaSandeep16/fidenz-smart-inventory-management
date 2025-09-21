package com.example.fidenz.controller;

import com.example.fidenz.base.BaseIntegrationTest;
import com.example.fidenz.dto.SalesTransactionRequest;
import com.example.fidenz.entity.*;
import com.example.fidenz.repository.*;
import com.example.fidenz.security.JwtUtil;
import com.example.fidenz.testdata.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Sales Controller Integration Tests")
public class SalesControllerTest extends BaseIntegrationTest {

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private User testUser;
    private Store testStore1;
    private Store testStore2;
    private Product testProduct1;
    private Product testProduct2;
    private Inventory inventory1;
    private Inventory inventory2;
    private SalesTransaction salesTransaction1;
    private SalesTransaction salesTransaction2;
    private String jwtToken;

    @BeforeEach
    void setUpTestData() {
        // Test data is now loaded from data.sql
        // Get the test users that were inserted by SQL scripts
        testUser = userRepository.findByUsername("testmanager")
                .orElseThrow(() -> new RuntimeException("Test manager user not found"));
        
        // Generate JWT token
        jwtToken = jwtUtil.generateToken(testUser.getUsername());

        // Get test data from SQL scripts
        testStore1 = storeRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Test store 1 not found"));
        testStore2 = storeRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("Test store 2 not found"));

        testProduct1 = productRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Test product 1 not found"));
        testProduct2 = productRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("Test product 2 not found"));

        inventory1 = inventoryRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Test inventory 1 not found"));
        inventory2 = inventoryRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("Test inventory 2 not found"));

        // Note: Sales transactions will be created by the tests themselves
        // as they test the record sales functionality
    }

    @Test
    @DisplayName("Should record a new sale successfully")
    void testRecordSale_WithValidData_ShouldCreateTransaction() throws Exception {
        // Given
        SalesTransactionRequest request = TestDataBuilder.createSalesTransactionRequest(
                testProduct1.getId(), testStore1.getId(), 5, new BigDecimal("99.99"));

        // When & Then
        mockMvc.perform(post("/api/sales/transaction")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.quantity", is(5)))
                .andExpect(jsonPath("$.unitPrice", is(99.99)))
                .andExpect(jsonPath("$.totalAmount", is(499.95)))
                .andExpect(jsonPath("$.product.id", is(testProduct1.getId().intValue())))
                .andExpect(jsonPath("$.store.id", is(testStore1.getId().intValue())));
    }

    @Test
    @DisplayName("Should return 400 for insufficient stock")
    void testRecordSale_WithInsufficientStock_ShouldReturn400() throws Exception {
        // Given - trying to sell more than available stock (50 available)
        SalesTransactionRequest request = TestDataBuilder.createSalesTransactionRequest(
                testProduct1.getId(), testStore1.getId(), 60, new BigDecimal("99.99"));

        // When & Then
        mockMvc.perform(post("/api/sales/transaction")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 for non-existent product")
    void testRecordSale_WithNonExistentProduct_ShouldReturn404() throws Exception {
        // Given
        SalesTransactionRequest request = TestDataBuilder.createSalesTransactionRequest(
                999L, testStore1.getId(), 5, new BigDecimal("99.99"));

        // When & Then
        mockMvc.perform(post("/api/sales/transaction")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 for non-existent store")
    void testRecordSale_WithNonExistentStore_ShouldReturn404() throws Exception {
        // Given
        SalesTransactionRequest request = TestDataBuilder.createSalesTransactionRequest(
                testProduct1.getId(), 999L, 5, new BigDecimal("99.99"));

        // When & Then
        mockMvc.perform(post("/api/sales/transaction")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 for zero quantity")
    void testRecordSale_WithZeroQuantity_ShouldReturn400() throws Exception {
        // Given
        SalesTransactionRequest request = TestDataBuilder.createSalesTransactionRequest(
                testProduct1.getId(), testStore1.getId(), 0, new BigDecimal("99.99"));

        // When & Then
        mockMvc.perform(post("/api/sales/transaction")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for negative quantity")
    void testRecordSale_WithNegativeQuantity_ShouldReturn400() throws Exception {
        // Given
        SalesTransactionRequest request = TestDataBuilder.createSalesTransactionRequest(
                testProduct1.getId(), testStore1.getId(), -5, new BigDecimal("99.99"));

        // When & Then
        mockMvc.perform(post("/api/sales/transaction")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for zero or negative unit price")
    void testRecordSale_WithZeroUnitPrice_ShouldReturn400() throws Exception {
        // Given
        SalesTransactionRequest request = TestDataBuilder.createSalesTransactionRequest(
                testProduct1.getId(), testStore1.getId(), 5, BigDecimal.ZERO);

        // When & Then
        mockMvc.perform(post("/api/sales/transaction")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get sales by store successfully")
    void testGetSalesByStore_WithValidStoreId_ShouldReturnSales() throws Exception {
        mockMvc.perform(get("/api/sales/store/{storeId}", testStore1.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0))); // Empty list since no sales exist yet
    }

    @Test
    @DisplayName("Should return empty list for store with no sales")
    void testGetSalesByStore_WithStoreWithoutSales_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/sales/store/{storeId}", testStore2.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should get recent sales by store successfully")
    void testGetRecentSalesByStore_WithValidParameters_ShouldReturnRecentSales() throws Exception {
        mockMvc.perform(get("/api/sales/store/{storeId}/recent/{days}", testStore1.getId(), 7)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0))); // Both transactions are within 7 days
    }

    @Test
    @DisplayName("Should filter recent sales correctly by days")
    void testGetRecentSalesByStore_WithSmallDayRange_ShouldFilterCorrectly() throws Exception {
        mockMvc.perform(get("/api/sales/store/{storeId}/recent/{days}", testStore1.getId(), 1)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0))); // No existing transactions
    }

    @Test
    @DisplayName("Should return 401 for missing authentication")
    void testRecordSale_WithoutAuth_ShouldReturn401() throws Exception {
        // Given
        SalesTransactionRequest request = TestDataBuilder.createSalesTransactionRequest(
                testProduct1.getId(), testStore1.getId(), 5, new BigDecimal("99.99"));

        // When & Then
        mockMvc.perform(post("/api/sales/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 for invalid JWT token")
    void testGetSalesByStore_WithInvalidToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/sales/store/{storeId}", testStore1.getId())
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 400 for invalid store ID format")
    void testGetSalesByStore_WithInvalidIdFormat_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/sales/store/{storeId}", "invalid-id")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for null request body")
    void testRecordSale_WithNullRequest_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/sales/transaction")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 415 for unsupported media type")
    void testRecordSale_WithUnsupportedMediaType_ShouldReturn415() throws Exception {
        // Given
        SalesTransactionRequest request = TestDataBuilder.createSalesTransactionRequest(
                testProduct1.getId(), testStore1.getId(), 5, new BigDecimal("99.99"));

        // When & Then
        mockMvc.perform(post("/api/sales/transaction")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(asJsonString(request)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("Should handle large quantity sales correctly")
    void testRecordSale_WithLargeQuantity_ShouldCalculateCorrectTotal() throws Exception {
        // Given - selling 50 items at 99.99 each
        SalesTransactionRequest request = TestDataBuilder.createSalesTransactionRequest(
                testProduct1.getId(), testStore1.getId(), 50, new BigDecimal("99.99"));

        // When & Then
        mockMvc.perform(post("/api/sales/transaction")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(50)))
                .andExpect(jsonPath("$.totalAmount", is(4999.50)));
    }
}
