package com.example.fidenz.repository;

import com.example.fidenz.entity.Product;
import com.example.fidenz.entity.ReorderRecommendation;
import com.example.fidenz.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReorderRecommendationRepository extends JpaRepository<ReorderRecommendation, Long> {

    Optional<ReorderRecommendation> findByProductAndStore(Product product, Store store);
    
    // JOIN FETCH queries for better performance with LAZY loading
    @Query("SELECT r FROM ReorderRecommendation r JOIN FETCH r.product JOIN FETCH r.store WHERE r.store.id = :storeId AND r.processed = :processed")
    List<ReorderRecommendation> findByStoreIdAndProcessedWithDetails(@Param("storeId") Long storeId, @Param("processed") Boolean processed);

}
