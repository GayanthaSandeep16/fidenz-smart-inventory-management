package com.example.fidenz.service;

import com.example.fidenz.entity.Inventory;
import com.example.fidenz.entity.Store;
import com.example.fidenz.exception.EntityNotFoundException;
import com.example.fidenz.repository.InventoryRepository;
import com.example.fidenz.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This service handles inventory-related operations such as
 * fetching and updating inventory data.
 *
 */

@Service
public class InventoryService {


    private final InventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;

    public InventoryService(InventoryRepository inventoryRepository, StoreRepository storeRepository) {
        this.inventoryRepository = inventoryRepository;
        this.storeRepository = storeRepository;
    }

    public List<Inventory> getInventoryByStore(Long storeId) {
        // Validate store exists
        storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store", storeId));
        
        // Use JOIN FETCH for better performance with LAZY loading
        return inventoryRepository.findByStoreIdWithDetails(storeId);
    }

    public Inventory updateInventory(Long inventoryId, Integer newStock) {
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        
        // Use JOIN FETCH to load related entities if needed for response
        Inventory inventory = inventoryRepository.findByIdWithDetails(inventoryId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory", inventoryId));
        
        inventory.setCurrentStock(newStock);
        return inventoryRepository.save(inventory);
    }

    public List<Inventory> getAllInventories() {
        // Use JOIN FETCH for better performance with LAZY loading
        return inventoryRepository.findAllWithDetails();
    }
}
