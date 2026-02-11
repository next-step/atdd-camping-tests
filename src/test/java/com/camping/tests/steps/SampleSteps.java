package com.camping.tests.steps;

import com.camping.tests.api.GenericApi;
import com.camping.tests.steps.context.TestContext;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만약;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SampleSteps {

    @Autowired
    private TestContext testContext;

    @Autowired
    private GenericApi genericApi;

    @만약("{string}에 요청을 보낸다")
    public void 요청을보낸다(String url) {
        testContext.setResponse(genericApi.전체_URL로_GET_요청(url));
    }

    @그러면("성공 응답을 받는다")
    public void 성공응답을받는다() {
        assertEquals(200, testContext.getResponse().statusCode());
    }
}
