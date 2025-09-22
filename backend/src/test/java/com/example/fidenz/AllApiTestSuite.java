package com.example.fidenz;

import com.example.fidenz.controller.AlgorithmControllerTest;
import com.example.fidenz.controller.AuthControllerTest;
import com.example.fidenz.controller.InventoryControllerTest;
import com.example.fidenz.controller.SalesControllerTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite that runs all API integration tests
 */
@Suite
@SuiteDisplayName("All API Integration Tests")
@SelectClasses({
    AuthControllerTest.class,
    InventoryControllerTest.class,
    SalesControllerTest.class,
    AlgorithmControllerTest.class
})
public class AllApiTestSuite {
    // Test suite configuration class
}

