package com.camping.tests.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.camping.tests.fixture.PaymentTestFixture.*;

public class PaymentSteps {

    private List<Map<String, Object>> selectedItems = new ArrayList<>();
    private ExtractableResponse<Response> paymentResponse;

    @Given("상품 목록에서 결제할 상품을 선택한다")
    public void 상품목록에서결제할상품을선택한다(DataTable dataTable) {
        List<Map<String, String>> items = dataTable.asMaps();
        selectedItems = 상품_목록_생성(items);
    }

    @When("정상 금액으로 결제를 요청한다")
    public void 정상금액으로결제를요청한다() {
        paymentResponse = 정상_금액으로_결제_요청(selectedItems);
    }

    @When("유효하지 않은 금액으로 결제를 요청한다")
    public void 유효하지않은금액으로결제를요청한다() {
        paymentResponse = 유효하지_않은_금액으로_결제_요청();
    }

    @Then("결제가 성공한다")
    public void 결제가성공한다() {
        결제_성공_검증(paymentResponse);
    }

    @Then("결제가 실패한다")
    public void 결제가실패한다() {
        결제_실패_검증(paymentResponse);
    }

    @And("결제 응답에 paymentKey가 포함되어 있다")
    public void 결제응답에paymentKey가포함되어있다() {
        paymentKey_포함_검증(paymentResponse);
    }

    @And("결제 응답에 orderId가 포함되어 있다")
    public void 결제응답에orderId가포함되어있다() {
        orderId_포함_검증(paymentResponse);
    }

    @And("실패 메시지가 {string}이다")
    public void 실패메시지가이다(String expectedMessage) {
        실패_메시지_검증(paymentResponse, expectedMessage);
    }
}