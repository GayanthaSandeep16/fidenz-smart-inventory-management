package com.example.fidenz.repository;

import com.example.fidenz.entity.Product;
import com.example.fidenz.entity.ReorderRecommendation;
import com.example.fidenz.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReorderRecommendationRepository extends JpaRepository<ReorderRecommendation, Long> {

    List<ReorderRecommendation> findByStoreIdAndIsProcessed(Long storeId, Boolean isProcessed);
    
    Optional<ReorderRecommendation> findByProductAndStore(Product product, Store store);
}
