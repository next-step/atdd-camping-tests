package com.camping.tests.scenario;

import static com.camping.tests.steps.admin.AdminProductTestSteps.상품_등록이_성공한다;
import static com.camping.tests.steps.admin.AdminProductTestSteps.상품_이름을_수정한다;
import static com.camping.tests.steps.admin.AdminProductTestSteps.상품_정보_수정이_성공한다;
import static com.camping.tests.steps.admin.AdminProductTestSteps.상품_정보를_가져온다;
import static com.camping.tests.steps.admin.AdminProductTestSteps.상품을_등록한다;
import static com.camping.tests.steps.kiosk.KioskProductTestSteps.상품_목록에서_상품이_조회되지_않는다;
import static com.camping.tests.steps.kiosk.KioskProductTestSteps.상품_목록에서_상품이_조회된다;

import com.camping.tests.steps.admin.dto.CreateAdminProductRequest;
import com.camping.tests.steps.admin.dto.UpdateAdminProductRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class ProductScenarioSteps {
    private Response 상품_등록_응답;
    private Response 상품_수정_응답;

    // 상품 등록
    @Given("어드민에서 {string} 상품이 등록되어 있다")
    public void 어드민에서_상품이_등록되어_있다(String 상품명) {
        어드민에서_상품을_등록한다(상품명);
        어드민에서_상품_등록이_성공한다();
    }

    @When("어드민에서 {string} 상품을 등록한다")
    public void 어드민에서_상품을_등록한다(String 상품명) {
        상품_등록_응답 = 상품을_등록한다(CreateAdminProductRequest.fixture()
            .name(상품명)
            .create());
    }

    @Then("어드민에서 상품 등록이 성공한다")
    public void 어드민에서_상품_등록이_성공한다() {
        상품_등록이_성공한다(상품_등록_응답);
    }

    // 상품 수정
    @When("어드민에서 상품 이름을 {string}으로 수정한다")
    public void 어드민에서_상품_이름을_수정한다(String 상품명) {
        var 상품_정보 = 상품_정보를_가져온다(상품_등록_응답);

        상품_수정_응답 = 상품_이름을_수정한다(
            상품_정보.id(),
            UpdateAdminProductRequest.fixture()
                .name(상품명)
                .create()
        );
    }

    @Then("어드민에서 상품 정보 수정이 성공한다")
    public void 어드민에서_상품_정보_수정이_성공한다() {
        상품_정보_수정이_성공한다(상품_수정_응답);
    }

    // 상품 조회
    @Then("키오스크에서 {string} 상품이 조회된다")
    public void 키오스크에서_상품이_조회된다(String 상품명) {
        상품_목록에서_상품이_조회된다(상품명);
    }

    @Then("키오스크에서 {string} 상품이 조회되지 않는다")
    public void 키오스크에서_상품이_조회되지_않는다(String 상품명) {
        상품_목록에서_상품이_조회되지_않는다(상품명);
    }
}
