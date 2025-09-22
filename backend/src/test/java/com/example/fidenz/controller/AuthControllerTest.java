package com.example.fidenz.controller;

import com.example.fidenz.base.BaseIntegrationTest;
import com.example.fidenz.dto.AuthRequest;
import com.example.fidenz.entity.User;
import com.example.fidenz.repository.UserRepository;
import com.example.fidenz.testdata.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Auth Controller Integration Tests")
public class AuthControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User testEmployee;

    @BeforeEach
    void setUpTestData() {
        // Get the test users that were inserted by SQL scripts
        testUser = userRepository.findByUsername("testmanager")
                .orElseThrow(() -> new RuntimeException("Test manager user not found"));
        
        testEmployee = userRepository.findByUsername("testoperator")
                .orElseThrow(() -> new RuntimeException("Test operator user not found"));
    }

    @Test
    @DisplayName("Should successfully login with valid credentials - Store Manager")
    void testLogin_WithValidCredentials_StoreManager_ShouldReturnToken() throws Exception {
        // Given
        AuthRequest authRequest = TestDataBuilder.createAuthRequest("testmanager", "password");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", is(notNullValue())))
                .andExpect(jsonPath("$.username", is("testmanager")))
                .andExpect(jsonPath("$.role", is("STORE_MANAGER")));
    }

    @Test
    @DisplayName("Should successfully login with valid credentials - Store Employee")
    void testLogin_WithValidCredentials_StoreEmployee_ShouldReturnToken() throws Exception {
        // Given
        AuthRequest authRequest = TestDataBuilder.createAuthRequest("testoperator", "password");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", is(notNullValue())))
                .andExpect(jsonPath("$.username", is("testoperator")))
                .andExpect(jsonPath("$.role", is("STORE_OPERATOR")));
    }

    @Test
    @DisplayName("Should return 401 for invalid username")
    void testLogin_WithInvalidUsername_ShouldReturn401() throws Exception {
        // Given
        AuthRequest authRequest = TestDataBuilder.createAuthRequest("invaliduser", "password");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(authRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 for invalid password")
    void testLogin_WithInvalidPassword_ShouldReturn401() throws Exception {
        // Given
        AuthRequest authRequest = TestDataBuilder.createAuthRequest("testmanager", "wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(authRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 400 for empty username")
    void testLogin_WithEmptyUsername_ShouldReturn400() throws Exception {
        // Given
        AuthRequest authRequest = TestDataBuilder.createAuthRequest("", "password");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(authRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for empty password")
    void testLogin_WithEmptyPassword_ShouldReturn400() throws Exception {
        // Given
        AuthRequest authRequest = TestDataBuilder.createAuthRequest("testmanager", "");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(authRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for null username")
    void testLogin_WithNullUsername_ShouldReturn400() throws Exception {
        // Given
        AuthRequest authRequest = TestDataBuilder.createAuthRequest(null, "password");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(authRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for null password")
    void testLogin_WithNullPassword_ShouldReturn400() throws Exception {
        // Given
        AuthRequest authRequest = TestDataBuilder.createAuthRequest("testmanager", null);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(authRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for malformed JSON")
    void testLogin_WithMalformedJson_ShouldReturn400() throws Exception {
        // Given
        String malformedJson = "{\"username\":\"testmanager\",\"password\":}";

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 415 for unsupported media type")
    void testLogin_WithUnsupportedMediaType_ShouldReturn415() throws Exception {
        // Given
        AuthRequest authRequest = TestDataBuilder.createAuthRequest("testmanager", "password");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(asJsonString(authRequest)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("Should return 405 for unsupported HTTP method")
    void testLogin_WithGetMethod_ShouldReturn405() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); // Bad request due to missing body
    }
}

