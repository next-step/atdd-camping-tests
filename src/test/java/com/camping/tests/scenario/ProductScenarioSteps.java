package com.camping.tests.scenario;

import static com.camping.tests.steps.admin.AdminProductTestSteps.상품_등록이_성공한다;
import static com.camping.tests.steps.admin.AdminProductTestSteps.상품을_등록한다;
import static com.camping.tests.steps.kiosk.KioskProductTestSteps.상품_목록에서_상품이_조회된다;

import com.camping.tests.steps.admin.dto.CreateProductRequest;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class ProductScenarioSteps {
    private Response 상품_등록_응답;

    @When("어드민에서 {string} 상품을 등록한다")
    public void 어드민에서_상품을_등록한다(String 상품명) {
        상품_등록_응답 = 상품을_등록한다(CreateProductRequest.fixture()
            .name(상품명)
            .create());
    }

    @Then("어드민에서 상품 등록이 성공한다")
    public void 어드민에서_상품_등록이_성공한다() {
        상품_등록이_성공한다(상품_등록_응답);
    }

    @Then("키오스크에서 {string} 상품이 조회된다")
    public void 키오스크에서_상품이_조회된다(String 상품명) {
        상품_목록에서_상품이_조회된다(상품명);
    }
}
