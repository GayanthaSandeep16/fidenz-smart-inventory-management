package com.example.fidenz.dto;

import com.example.fidenz.entity.Inventory;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Mapper utility to convert Inventory entities to DTOs
 */
public class InventoryMapper {

    public static InventoryResponse toResponse(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        
        return new InventoryResponse(
            inventory.getId(),
            inventory.getCurrentStock(),
            inventory.getCreatedAt(),
            inventory.getUpdatedAt(),
            inventory.getProduct() != null ? inventory.getProduct().getId() : null,
            inventory.getProduct() != null ? inventory.getProduct().getName() : null,
            inventory.getProduct() != null ? inventory.getProduct().getCategory() : null,
            inventory.getProduct() != null ? inventory.getProduct().getSku() : null,
            inventory.getStore() != null ? inventory.getStore().getId() : null,
            inventory.getStore() != null ? inventory.getStore().getName() : null,
            inventory.getStore() != null ? inventory.getStore().getLocation() : null
        );
    }

    public static List<InventoryResponse> toResponseList(List<Inventory> inventories) {
        if (inventories == null) {
            return null;
        }
        
        return inventories.stream()
                .map(InventoryMapper::toResponse)
                .collect(Collectors.toList());
    }
}
