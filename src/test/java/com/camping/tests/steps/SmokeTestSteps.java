package com.camping.tests.steps;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class SmokeTestSteps {
    private Response serviceResponse;

    @ParameterType("KIOSK|ADMIN|RESERVATION")
    public Service service(String serviceName) {
        return Service.valueOf(serviceName);
    }

    @When("{service} 서비스에 요청을 보낸다")
    public void XX_서비스에_요청을_보낸다(Service service) {
        var healthCheckUrl = getHealthCheckUrl(service);

        serviceResponse = given()
            .when().get(healthCheckUrl)
            .thenReturn();
    }

    private static String getHealthCheckUrl(Service service) {
        var healthCheckUrl = service.getBaseUrl();
        if (service == Service.ADMIN) {
            healthCheckUrl += "/login"; // admin page는 인가 처리로 인해 login 페이지로 요청
        }
        return healthCheckUrl;
    }

    @Then("200 응답을 받는다")
    public void _200_응답을_받는다() {
        assertThat(serviceResponse.statusCode()).isEqualTo(200);
    }
}
