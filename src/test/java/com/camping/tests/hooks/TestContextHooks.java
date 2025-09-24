package com.camping.tests.hooks;

import com.camping.tests.scenario.TestContext;
import io.cucumber.java.BeforeAll;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestContextHooks {
    @BeforeAll
    public static void beforeAll() {
        log.info("=== Test Suite Started ===");
        TestContext.clear();
    }
}
