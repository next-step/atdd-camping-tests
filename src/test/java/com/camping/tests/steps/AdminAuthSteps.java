package com.camping.tests.steps;
import com.camping.tests.context.AdminContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import static com.camping.tests.config.ExternalAPIConfig.*;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
public class AdminAuthSteps {
    private final AdminContext adminContext;
    private ExtractableResponse<Response> response;

    public AdminAuthSteps(AdminContext adminContext) {
        this.adminContext = adminContext;
    }

    @Given("로그인을 한다")
    public void 로그인을_한다() {
        if (adminContext.getAdminToken() != null) {
            return;
        }
        String token = fetchAdminToken();
        adminContext.setAdminToken(token);
    }

    @When("어드민에서 유효한 계정으로 로그인하면")
    public void 어드민에서_유효한_계정으로_로그인하면() {
        String username = getAdminUsername();
        String password = getAdminPassword();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(adminHost() + "/auth/login")
                .then().log().all()
                .extract();
    }

    private static String fetchAdminToken() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", getAdminUsername());
        loginRequest.put("password", getAdminPassword());

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post(getServiceHost("어드민시스템") + "/auth/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("accessToken");
    }

    @Then("로그인에 성공한다")
    public void 로그인에_성공한다() {
        assertThat(response.statusCode()).isEqualTo(200);
    }
    @Then("인증 토큰이 발급된다")
    public void 인증_토큰이_발급된다() {
        String token = response.jsonPath().getString("accessToken");
        assertThat(token).isNotBlank();
    }
}
