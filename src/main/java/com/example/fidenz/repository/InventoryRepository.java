package com.example.fidenz.repository;

import com.example.fidenz.entity.Inventory;
import com.example.fidenz.entity.Product;
import com.example.fidenz.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductAndStore(Product product, Store store);
    
    // JOIN FETCH queries for better performance with LAZY loading
    @Query("SELECT i FROM Inventory i JOIN FETCH i.product JOIN FETCH i.store WHERE i.store.id = :storeId")
    List<Inventory> findByStoreIdWithDetails(@Param("storeId") Long storeId);
    
    @Query("SELECT i FROM Inventory i JOIN FETCH i.product JOIN FETCH i.store")
    List<Inventory> findAllWithDetails();
    
    @Query("SELECT i FROM Inventory i JOIN FETCH i.product JOIN FETCH i.store WHERE i.id = :inventoryId")
    Optional<Inventory> findByIdWithDetails(@Param("inventoryId") Long inventoryId);

}
