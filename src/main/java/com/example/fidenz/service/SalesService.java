package com.example.fidenz.service;

import com.example.fidenz.dto.SalesTransactionRequest;
import com.example.fidenz.entity.Inventory;
import com.example.fidenz.entity.Product;
import com.example.fidenz.entity.SalesTransaction;
import com.example.fidenz.entity.Store;
import com.example.fidenz.exception.EntityNotFoundException;
import com.example.fidenz.repository.InventoryRepository;
import com.example.fidenz.repository.ProductRepository;
import com.example.fidenz.repository.SalesTransactionRepository;
import com.example.fidenz.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SalesService {

    private static final Logger log = LoggerFactory.getLogger(SalesService.class);

    private final SalesTransactionRepository salesTransactionRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    public SalesService(SalesTransactionRepository salesTransactionRepository, InventoryRepository inventoryRepository,
                       ProductRepository productRepository, StoreRepository storeRepository) {
        this.salesTransactionRepository = salesTransactionRepository;
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
    }

    @Transactional
    public SalesTransaction recordSale(SalesTransactionRequest request) {
        // Validate product and store exist
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product", request.productId()));
        
        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new EntityNotFoundException("Store", request.storeId()));

        // Check inventory availability
        Inventory inventory = inventoryRepository.findByProductAndStore(product, store)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found for this product and store"));

        if (inventory.getCurrentStock() < request.quantity()) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + inventory.getCurrentStock());
        }

        // Calculate total amount
        BigDecimal totalAmount = request.unitPrice().multiply(BigDecimal.valueOf(request.quantity()));

        // Create sales transaction
        SalesTransaction transaction = new SalesTransaction();
        transaction.setProduct(product);
        transaction.setStore(store);
        transaction.setQuantity(request.quantity());
        transaction.setUnitPrice(request.unitPrice());
        transaction.setTotalAmount(totalAmount);
        transaction.setTransactionDate(LocalDateTime.now());

        // Save transaction
        SalesTransaction savedTransaction = salesTransactionRepository.save(transaction);

        // Update inventory
        inventory.setCurrentStock(inventory.getCurrentStock() - request.quantity());
        inventoryRepository.save(inventory);

        return savedTransaction;
    }

    public List<SalesTransaction> getSalesByStore(Long storeId) {
        return salesTransactionRepository.findByStoreId(storeId);
    }

    public List<SalesTransaction> getRecentSalesByStore(Long storeId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return salesTransactionRepository.findRecentSalesByStore(storeId, startDate);
    }
}
