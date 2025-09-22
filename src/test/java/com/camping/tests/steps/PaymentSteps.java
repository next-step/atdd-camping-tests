package com.camping.tests.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentSteps {

    private List<Map<String, Object>> selectedItems = new ArrayList<>();
    private ExtractableResponse<Response> paymentResponse;

    @Given("상품 목록에서 결제할 상품을 선택한다")
    public void 상품목록에서결제할상품을선택한다(DataTable dataTable) {
        selectedItems.clear();
        List<Map<String, String>> items = dataTable.asMaps();

        for (Map<String, String> item : items) {
            Map<String, Object> selectedItem = new HashMap<>();
            selectedItem.put("productId", Long.parseLong(item.get("productId")));
            selectedItem.put("quantity", Integer.parseInt(item.get("quantity")));
            selectedItem.put("price", Integer.parseInt(item.get("price")));
            selectedItems.add(selectedItem);
        }
    }

    @When("정상 금액으로 결제를 요청한다")
    public void 정상금액으로결제를요청한다() {
        // CartItem 구조에 맞게 변환
        List<Map<String, Object>> cartItems = new ArrayList<>();
        for (Map<String, Object> item : selectedItems) {
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("productId", item.get("productId"));
            cartItem.put("productName", "Test Product " + item.get("productId"));
            cartItem.put("unitPrice", item.get("price"));
            cartItem.put("quantity", item.get("quantity"));
            cartItems.add(cartItem);
        }

        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("items", cartItems);
        paymentRequest.put("paymentMethod", "CARD");

        paymentResponse = RestAssured.given()
                .log().all()
                .contentType("application/json")
                .body(paymentRequest)
                .when()
                .post("http://localhost:18081/api/payments")
                .then()
                .log().all()
                .extract();
    }

    @When("유효하지 않은 금액으로 결제를 요청한다")
    public void 유효하지않은금액으로결제를요청한다() {
        // amount = 0이 되도록 0원 상품 설정
        List<Map<String, Object>> cartItems = new ArrayList<>();
        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("productId", 998L);
        cartItem.put("productName", "Zero Price Product");
        cartItem.put("unitPrice", 0); // 0원으로 설정하여 에러 유발
        cartItem.put("quantity", 1);
        cartItems.add(cartItem);

        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("items", cartItems);
        paymentRequest.put("paymentMethod", "CARD");

        paymentResponse = RestAssured.given()
                .log().all()
                .contentType("application/json")
                .body(paymentRequest)
                .when()
                .post("http://localhost:18081/api/payments")
                .then()
                .log().all()
                .extract();
    }

    @Then("결제가 성공한다")
    public void 결제가성공한다() {
        assertThat(paymentResponse.statusCode()).isEqualTo(200);
        assertThat((Boolean) paymentResponse.jsonPath().getBoolean("success")).isTrue();
    }

    @Then("결제가 실패한다")
    public void 결제가실패한다() {
        assertThat(paymentResponse.statusCode()).isEqualTo(200);
        JsonPath jsonPath = paymentResponse.jsonPath();
        Boolean success = jsonPath.getBoolean("success");
        assertThat(success).isFalse();
    }

    @And("결제 응답에 paymentKey가 포함되어 있다")
    public void 결제응답에paymentKey가포함되어있다() {
        String paymentKey = paymentResponse.jsonPath().getString("paymentKey");
        assertThat(paymentKey).isNotNull();
        assertThat(paymentKey).isNotEmpty();
    }

    @And("결제 응답에 orderId가 포함되어 있다")
    public void 결제응답에orderId가포함되어있다() {
        String orderId = paymentResponse.jsonPath().getString("orderId");
        assertThat(orderId).isNotNull();
    }

    @And("실패 메시지가 {string}이다")
    public void 실패메시지가이다(String expectedMessage) {
        JsonPath jsonPath = paymentResponse.jsonPath();
        String actualMessage = jsonPath.getString("message");
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }
}