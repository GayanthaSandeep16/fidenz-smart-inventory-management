package com.example.fidenz.controller;

import com.example.fidenz.dto.SalesTransactionRequest;
import com.example.fidenz.entity.SalesTransaction;
import com.example.fidenz.service.SalesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@Tag(name = "Sales", description = "Sales transaction management APIs")
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    @PostMapping("/transaction")
    @Operation(summary = "Record a new sale", description = "Record a new sales transaction and update inventory")
    @ApiResponse(responseCode = "200", description = "Sale recorded successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or insufficient stock")
    public ResponseEntity<SalesTransaction> recordSale(@Valid @RequestBody SalesTransactionRequest request) {
        SalesTransaction transaction = salesService.recordSale(request);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get sales by store", description = "Retrieve all sales transactions for a specific store")
    @ApiResponse(responseCode = "200", description = "Sales retrieved successfully")
    public ResponseEntity<List<SalesTransaction>> getSalesByStore(@PathVariable Long storeId) {
        List<SalesTransaction> sales = salesService.getSalesByStore(storeId);
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/store/{storeId}/recent/{days}")
    @Operation(summary = "Get recent sales by store", description = "Retrieve recent sales transactions for a specific store")
    @ApiResponse(responseCode = "200", description = "Recent sales retrieved successfully")
    public ResponseEntity<List<SalesTransaction>> getRecentSalesByStore(
            @PathVariable Long storeId, 
            @PathVariable int days) {
        List<SalesTransaction> sales = salesService.getRecentSalesByStore(storeId, days);
        return ResponseEntity.ok(sales);
    }
}
