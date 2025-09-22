package com.example.fidenz.testdata;

import com.example.fidenz.dto.AuthRequest;
import com.example.fidenz.dto.SalesTransactionRequest;
import com.example.fidenz.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TestDataBuilder {


    public static SalesTransaction createTestSalesTransaction(Product product, Store store, Integer quantity, BigDecimal unitPrice) {
        SalesTransaction transaction = new SalesTransaction();
        transaction.setId(1L);
        transaction.setProduct(product);
        transaction.setStore(store);
        transaction.setQuantity(quantity);
        transaction.setUnitPrice(unitPrice);
        transaction.setTotalAmount(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        return transaction;
    }

    public static AuthRequest createAuthRequest(String username, String password) {
        return new AuthRequest(username, password);
    }

    public static SalesTransactionRequest createSalesTransactionRequest(Long productId, Long storeId, Integer quantity, BigDecimal unitPrice) {
        return new SalesTransactionRequest(productId, storeId, quantity, unitPrice);
    }

}
