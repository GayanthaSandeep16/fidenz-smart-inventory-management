package com.example.fidenz.repository;

import com.example.fidenz.entity.Inventory;
import com.example.fidenz.entity.Product;
import com.example.fidenz.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    List<Inventory>  findByStore(Store store);

    Optional<Inventory> findByProductAndStore(Product product, Store store);
    

}
