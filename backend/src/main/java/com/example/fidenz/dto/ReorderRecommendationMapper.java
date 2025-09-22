package com.example.fidenz.dto;

import com.example.fidenz.entity.ReorderRecommendation;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility to convert ReorderRecommendation entities to DTOs
 */
public class ReorderRecommendationMapper {

    public static ReorderRecommendationResponse toResponse(ReorderRecommendation recommendation) {
        if (recommendation == null) {
            return null;
        }
        
        return new ReorderRecommendationResponse(
            recommendation.getId(),
            recommendation.getCurrentStock(),
            recommendation.getAverageDailySales(),
            recommendation.getSeasonalityFactor(),
            recommendation.getLeadTime(),
            recommendation.getSafetyStock(),
            recommendation.getReorderPoint(),
            recommendation.getRecommendedQuantity(),
            recommendation.getProcessed(),
            recommendation.getCreatedAt(),
            recommendation.getUpdatedAt(),
            recommendation.getProduct() != null ? recommendation.getProduct().getId() : null,
            recommendation.getProduct() != null ? recommendation.getProduct().getName() : null,
            recommendation.getProduct() != null ? recommendation.getProduct().getCategory() : null,
            recommendation.getProduct() != null ? recommendation.getProduct().getSku() : null,
            recommendation.getStore() != null ? recommendation.getStore().getId() : null,
            recommendation.getStore() != null ? recommendation.getStore().getName() : null,
            recommendation.getStore() != null ? recommendation.getStore().getLocation() : null
        );
    }

    public static List<ReorderRecommendationResponse> toResponseList(List<ReorderRecommendation> recommendations) {
        if (recommendations == null) {
            return null;
        }
        
        return recommendations.stream()
                .map(ReorderRecommendationMapper::toResponse)
                .collect(Collectors.toList());
    }
}
