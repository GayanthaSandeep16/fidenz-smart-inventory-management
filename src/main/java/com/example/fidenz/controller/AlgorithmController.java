package com.example.fidenz.controller;

import com.example.fidenz.dto.AbcAnalysisResult;
import com.example.fidenz.entity.ReorderRecommendation;
import com.example.fidenz.service.AbcAnalysisService;
import com.example.fidenz.service.ReorderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/algorithms")
@Tag(name = "Algorithms", description = "Smart algorithms for inventory management")
public class AlgorithmController {

    private final ReorderService reorderService;
    private final AbcAnalysisService abcAnalysisService;

    public AlgorithmController(ReorderService reorderService, AbcAnalysisService abcAnalysisService) {
        this.reorderService = reorderService;
        this.abcAnalysisService = abcAnalysisService;
    }

    @GetMapping("/reorder-recommendations/{storeId}")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @Operation(summary = "Generate reorder recommendations", 
               description = "Generate smart reorder recommendations for a specific store based on sales history and seasonality")
    @ApiResponse(responseCode = "200", description = "Reorder recommendations generated successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - Store Manager role required")
    public ResponseEntity<List<ReorderRecommendation>> generateReorderRecommendations(@PathVariable Long storeId) {
        List<ReorderRecommendation> recommendations = reorderService.generateReorderSuggestions(storeId);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/reorder-recommendations/{storeId}/pending")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @Operation(summary = "Get pending reorder recommendations", 
               description = "Retrieve pending reorder recommendations for a specific store")
    @ApiResponse(responseCode = "200", description = "Pending recommendations retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - Store Manager role required")
    public ResponseEntity<List<ReorderRecommendation>> getPendingReorderRecommendations(@PathVariable Long storeId) {
        List<ReorderRecommendation> recommendations = reorderService.getReorderRecommendations(storeId);
        return ResponseEntity.ok(recommendations);
    }

    @PutMapping("/reorder-recommendations/{recommendationId}/process")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @Operation(summary = "Mark recommendation as processed", 
               description = "Mark a reorder recommendation as processed")
    @ApiResponse(responseCode = "200", description = "Recommendation marked as processed")
    @ApiResponse(responseCode = "403", description = "Access denied - Store Manager role required")
    public ResponseEntity<Void> markRecommendationAsProcessed(@PathVariable Long recommendationId) {
        reorderService.markRecommendationAsProcessed(recommendationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/abc-analysis/{storeId}")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @Operation(summary = "Perform ABC analysis", 
               description = "Perform ABC analysis for products in a specific store based on revenue contribution")
    @ApiResponse(responseCode = "200", description = "ABC analysis completed successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - Store Manager role required")
    public ResponseEntity<List<AbcAnalysisResult>> performAbcAnalysis(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "90") int days) {
        List<AbcAnalysisResult> results = abcAnalysisService.performAbcAnalysis(storeId, days);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/abc-analysis/{storeId}/by-category")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @Operation(summary = "Get ABC analysis by category", 
               description = "Get ABC analysis results grouped by category (A, B, C)")
    @ApiResponse(responseCode = "200", description = "ABC analysis by category retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - Store Manager role required")
    public ResponseEntity<Map<String, List<AbcAnalysisResult>>> getAbcAnalysisByCategory(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "90") int days) {
        Map<String, List<AbcAnalysisResult>> results = abcAnalysisService.getAbcAnalysisByCategory(storeId, days);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/abc-analysis/{storeId}/summary")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @Operation(summary = "Get ABC analysis summary", 
               description = "Get summary of ABC analysis showing count of products in each category")
    @ApiResponse(responseCode = "200", description = "ABC analysis summary retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - Store Manager role required")
    public ResponseEntity<Map<String, Long>> getAbcAnalysisSummary(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "90") int days) {
        Map<String, Long> summary = abcAnalysisService.getAbcAnalysisSummary(storeId, days);
        return ResponseEntity.ok(summary);
    }
}
