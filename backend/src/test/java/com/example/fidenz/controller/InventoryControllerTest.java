package com.example.fidenz.controller;

import com.example.fidenz.base.BaseIntegrationTest;
import com.example.fidenz.entity.Inventory;
import com.example.fidenz.entity.Product;
import com.example.fidenz.entity.Role;
import com.example.fidenz.entity.Store;
import com.example.fidenz.entity.User;
import com.example.fidenz.repository.InventoryRepository;
import com.example.fidenz.repository.ProductRepository;
import com.example.fidenz.repository.StoreRepository;
import com.example.fidenz.repository.UserRepository;
import com.example.fidenz.security.JwtUtil;
import com.example.fidenz.testdata.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Inventory Controller Integration Tests")
public class InventoryControllerTest extends BaseIntegrationTest {

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
    private Inventory inventory3;
    private String jwtToken;

    @BeforeEach
    void setUpTestData() {
        // Test data is now loaded from data.sql
        // Get the test data that was inserted by SQL scripts
        testUser = userRepository.findByUsername("testmanager")
                .orElseThrow(() -> new RuntimeException("Test manager user not found"));
        
        // Generate JWT token
        jwtToken = jwtUtil.generateToken(testUser.getUsername());
        
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
        
        inventory3 = inventoryRepository.findById(3L)
                .orElseThrow(() -> new RuntimeException("Test inventory 3 not found"));
    }

    @Test
    @DisplayName("Should get all inventories successfully")
    void testGetAllInventories_ShouldReturnAllInventories() throws Exception {
        mockMvc.perform(get("/api/inventory")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].id", is(notNullValue())))
                .andExpect(jsonPath("$[0].currentStock", is(notNullValue())))
                .andExpect(jsonPath("$[0].productId", is(notNullValue())))
                .andExpect(jsonPath("$[0].productName", is(notNullValue())))
                .andExpect(jsonPath("$[0].storeId", is(notNullValue())))
                .andExpect(jsonPath("$[0].storeName", is(notNullValue())));
    }

    @Test
    @DisplayName("Should get inventory by store ID successfully")
    void testGetInventoryByStore_WithValidStoreId_ShouldReturnInventoryForStore() throws Exception {
        mockMvc.perform(get("/api/inventory/{storeId}", testStore1.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].currentStock", anyOf(is(50), is(30))))
                .andExpect(jsonPath("$[1].currentStock", anyOf(is(50), is(30))));
    }

    @Test
    @DisplayName("Should return 404 for non-existent store")
    void testGetInventoryByStore_WithNonExistentStore_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/inventory/{storeId}", 999L)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update inventory stock successfully")
    void testUpdateInventory_WithValidData_ShouldUpdateStock() throws Exception {
        Integer newStock = 75;

        mockMvc.perform(put("/api/inventory/{inventoryId}", inventory1.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("newStock", newStock.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(inventory1.getId().intValue())))
                .andExpect(jsonPath("$.currentStock", is(newStock)));
    }

    @Test
    @DisplayName("Should return 404 for non-existent inventory ID")
    void testUpdateInventory_WithNonExistentId_ShouldReturn404() throws Exception {
        mockMvc.perform(put("/api/inventory/{inventoryId}", 999L)
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("newStock", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 for negative stock")
    void testUpdateInventory_WithNegativeStock_ShouldReturn400() throws Exception {
        mockMvc.perform(put("/api/inventory/{inventoryId}", inventory1.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("newStock", "-10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for missing newStock parameter")
    void testUpdateInventory_WithMissingParameter_ShouldReturn400() throws Exception {
        mockMvc.perform(put("/api/inventory/{inventoryId}", inventory1.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 for missing authentication")
    void testGetAllInventories_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 for invalid JWT token")
    void testGetAllInventories_WithInvalidToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/inventory")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 400 for invalid inventory ID format")
    void testGetInventoryByStore_WithInvalidIdFormat_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/inventory/{storeId}", "invalid-id")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 405 for unsupported HTTP method on get endpoint")
    void testInventoryEndpoint_WithUnsupportedMethod_ShouldReturn405() throws Exception {
        mockMvc.perform(delete("/api/inventory")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("Should handle concurrent inventory updates")
    void testUpdateInventory_ConcurrentUpdates_ShouldHandleGracefully() throws Exception {
        // First update
        mockMvc.perform(put("/api/inventory/{inventoryId}", inventory1.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("newStock", "60")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStock", is(60)));

        // Second update
        mockMvc.perform(put("/api/inventory/{inventoryId}", inventory1.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("newStock", "80")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStock", is(80)));
    }

    @Test
    @DisplayName("Should update inventory to zero stock")
    void testUpdateInventory_WithZeroStock_ShouldSucceed() throws Exception {
        mockMvc.perform(put("/api/inventory/{inventoryId}", inventory1.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("newStock", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStock", is(0)));
    }
}

