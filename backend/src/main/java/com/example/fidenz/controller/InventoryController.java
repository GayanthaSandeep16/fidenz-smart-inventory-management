package com.example.fidenz.controller;

import com.example.fidenz.dto.InventoryMapper;
import com.example.fidenz.dto.InventoryResponse;
import com.example.fidenz.entity.Inventory;
import com.example.fidenz.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Inventory management APIs")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{storeId}")
    @Operation(summary = "Get inventory by store", description = "Retrieve inventory for a specific store")
    @ApiResponse(responseCode = "200", description = "Inventory retrieved successfully")
    public ResponseEntity<List<InventoryResponse>> getInventoryByStore(@PathVariable Long storeId) {
        List<Inventory> inventory = inventoryService.getInventoryByStore(storeId);
        List<InventoryResponse> response = InventoryMapper.toResponseList(inventory);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all inventories", description = "Retrieve all inventory records")
    @ApiResponse(responseCode = "200", description = "All inventories retrieved successfully")
    public ResponseEntity<List<InventoryResponse>> getAllInventories() {
        List<Inventory> inventories = inventoryService.getAllInventories();
        List<InventoryResponse> response = InventoryMapper.toResponseList(inventories);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{inventoryId}")
    @Operation(summary = "Update inventory stock", description = "Update stock quantity for a specific inventory item")
    @ApiResponse(responseCode = "200", description = "Inventory updated successfully")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable Long inventoryId, 
            @RequestParam Integer newStock) {
        Inventory inventory = inventoryService.updateInventory(inventoryId, newStock);
        InventoryResponse response = InventoryMapper.toResponse(inventory);
        return ResponseEntity.ok(response);
    }
}
