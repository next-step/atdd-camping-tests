package com.camping.tests.steps;

import com.camping.tests.support.TestContext;
import io.cucumber.java.ko.그러면;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommonSteps {

    @그러면("응답 상태 코드는 {int}이다")
    public void 응답_상태_코드_확인(int expectedStatus) {
        assertEquals(expectedStatus, TestContext.current().getLastResponse().getStatusCode());
    }
}
