package com.camping.tests.steps;

import com.camping.tests.context.AdminContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static com.camping.tests.config.ExternalAPIConfig.getServiceHost;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
public class SampleSteps {

    private ExtractableResponse<Response> response;

    private final AdminContext adminContext;

    public SampleSteps(AdminContext adminContext) {
        this.adminContext = adminContext;
    }

    @When("{string}에 요청을 보낸다")
    public void 요청을보낸다(String serviceName) {
        RequestSpecification request = RestAssured.given()
                .baseUri(getServiceHost(serviceName));

        if (adminContext.getAdminToken() != null) {
            request.header("Authorization", "Bearer " + adminContext.getAdminToken());
        }

        response = request
                .when()
                .get("/")
                .then()
                .extract();
    }

    @Then("성공 응답을 받는다")
    public void 성공응답을받는다() {
        assertThat(response.statusCode()).isEqualTo(200);
    }
}
