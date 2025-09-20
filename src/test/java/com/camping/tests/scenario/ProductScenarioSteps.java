package com.camping.tests.scenario;

import static com.camping.tests.steps.admin.AdminProductTestSteps.상품_등록이_성공한다;
import static com.camping.tests.steps.admin.AdminProductTestSteps.상품을_등록한다;
import static com.camping.tests.steps.kiosk.KioskProductTestSteps.상품_목록_조회가_성공한다;
import static com.camping.tests.steps.kiosk.KioskProductTestSteps.상품_목록에_상품이_있다;
import static com.camping.tests.steps.kiosk.KioskProductTestSteps.상품_목록을_조회한다;

import com.camping.tests.steps.admin.dto.CreateProductRequest;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class ProductScenarioSteps {
    private Response 상품_등록_응답;

    @When("어드민에서 상품을 등록한다")
    public void 어드민에서_상품을_등록한다() {
        // TODO 어떤 형태의 상품을 등록할지 시나리오에서 정의할 수 있도록 개선 필요
        상품_등록_응답 = 상품을_등록한다(CreateProductRequest.fixture()
            .name("텐트")
            .create());
    }

    @Then("어드민에서 상품 등록이 성공한다")
    public void 어드민에서_상품_등록이_성공한다() {
        상품_등록이_성공한다(상품_등록_응답);
    }

    @Then("키오스크에서 등록한 상품이 조회된다")
    public void 키오스크에서_등록한_상품이_조회된다() {
        var 상품_목록_조회_응답 = 상품_목록을_조회한다();
        상품_목록_조회가_성공한다(상품_목록_조회_응답);
        상품_목록에_상품이_있다(상품_목록_조회_응답, "텐트");
    }
}
