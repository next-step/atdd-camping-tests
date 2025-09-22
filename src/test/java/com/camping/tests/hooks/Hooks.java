package com.camping.tests.hooks;

import com.camping.tests.CommonContext;
import io.cucumber.java.Before;

public class Hooks {
    @Before
    public void beforeScenario() {
        System.out.println("시나리오 시작 전 실행");
        CommonContext.lastResponse = null;
    }
}
