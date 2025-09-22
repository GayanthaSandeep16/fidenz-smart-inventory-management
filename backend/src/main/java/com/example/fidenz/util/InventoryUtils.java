package com.example.fidenz.util;

/**
 * Utility class for inventory management.
 */
public final class InventoryUtils {

    private InventoryUtils() {}

    public static int calculateTargetStock(Integer maxStock, Integer minStock) {
        if (maxStock == null && minStock == null) {
            return 0;
        }
        int max = maxStock != null ? maxStock : 0;
        int min = minStock != null ? minStock : 0;
        return Math.min(max / 2, min * 3);
    }

    public static int decrementStock(int currentStock, int quantity) {
        int newStock = currentStock - quantity;
        if (newStock < 0) {
            newStock = 0;
        }
        return newStock;
    }
}
