package com.camping.tests.scenario.rental;

import com.camping.tests.scenario.TestContext;
import com.camping.tests.steps.admin.AdminRentalTestSteps;
import com.camping.tests.steps.admin.AdminProductTestSteps;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class RentalScenarioSteps {

    @When("관리자가 예약에 대해 {string} 상품을 {string}개 렌탈한다")
    public void 관리자가_예약에_대해_상품을_렌탈한다(String productName, String quantity) {
        // 먼저 상품 목록에서 상품 ID를 찾는다
        var 상품_목록_응답 = AdminProductTestSteps.모든_상품을_조회한다();
        var 상품_ID = AdminRentalTestSteps.상품_ID를_가져온다(상품_목록_응답, productName);
        TestContext.Rental.상품_ID(상품_ID);

        // 예약 ID와 상품 ID로 렌탈 생성
        var 예약_ID = TestContext.Reservation.예약_ID();
        var 렌탈_생성_응답 = AdminRentalTestSteps.렌탈을_생성한다(예약_ID, 상품_ID, Integer.parseInt(quantity));
        TestContext.Rental.렌탈_등록_응답(렌탈_생성_응답);

        // 렌탈 ID 저장
        var 렌탈_ID = AdminRentalTestSteps.렌탈_ID를_가져온다(렌탈_생성_응답);
        TestContext.Rental.렌탈_ID(렌탈_ID);
    }

    @Then("렌탈이 성공한다")
    public void 렌탈이_성공한다() {
        var 렌탈_생성_응답 = TestContext.Rental.렌탈_등록_응답();
        AdminRentalTestSteps.렌탈이_성공한다(렌탈_생성_응답);
    }

    @Then("렌탈 상품이 {string}로 등록된다")
    public void 렌탈_상품이_등록된다(String expectedProductName) {
        var 렌탈_생성_응답 = TestContext.Rental.렌탈_등록_응답();
        AdminRentalTestSteps.렌탈_상품이_등록되었다(렌탈_생성_응답, expectedProductName);
    }

    @When("관리자가 렌탈을 반납 처리한다")
    public void 관리자가_렌탈을_반납_처리한다() {
        var 렌탈_ID = TestContext.Rental.렌탈_ID();
        var 렌탈_반납_응답 = AdminRentalTestSteps.렌탈을_반납_처리한다(렌탈_ID);
        TestContext.Rental.렌탈_반납_응답(렌탈_반납_응답);
    }

    @Then("렌탈 반납이 성공한다")
    public void 렌탈_반납이_성공한다() {
        var 렌탈_반납_응답 = TestContext.Rental.렌탈_반납_응답();
        AdminRentalTestSteps.렌탈_반납이_성공한다(렌탈_반납_응답);
    }

    @Then("렌탈 상태가 {string}로 변경된다")
    public void 렌탈_상태가_변경된다(String expectedStatus) {
        var 렌탈_반납_응답 = TestContext.Rental.렌탈_반납_응답();
        if ("반납완료".equals(expectedStatus)) {
            AdminRentalTestSteps.렌탈_상태가_반납완료로_변경되었다(렌탈_반납_응답);
        }
    }

    @Then("{string} 상품의 재고가 {string}개로 감소한다")
    public void 상품의_재고가_감소한다(String productName, String expectedStock) {
        var 상품_목록_응답 = AdminProductTestSteps.모든_상품을_조회한다();
        AdminRentalTestSteps.상품_재고가_확인된다(상품_목록_응답, productName, Integer.parseInt(expectedStock));
    }

    @Then("{string} 상품의 재고가 {string}개로 복구된다")
    public void 상품의_재고가_복구된다(String productName, String expectedStock) {
        var 상품_목록_응답 = AdminProductTestSteps.모든_상품을_조회한다();
        AdminRentalTestSteps.상품_재고가_확인된다(상품_목록_응답, productName, Integer.parseInt(expectedStock));
    }
}