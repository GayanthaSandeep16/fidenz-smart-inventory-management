package com.example.fidenz.service;

import com.example.fidenz.util.InventoryUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryUtilsTest {

    @Test
    void calculateTargetStock_handlesNullsAndLogic() {
        assertEquals(0, InventoryUtils.calculateTargetStock(null, null));
        assertEquals(0, InventoryUtils.calculateTargetStock(0, 0));
        assertEquals(10, InventoryUtils.calculateTargetStock(20, 5));
        assertEquals(9, InventoryUtils.calculateTargetStock(18, 10));
    }

    @Test
    void decrementStock_neverBelowZero() {
        assertEquals(7, InventoryUtils.decrementStock(10, 3));
        assertEquals(0, InventoryUtils.decrementStock(2, 5));
    }
}
