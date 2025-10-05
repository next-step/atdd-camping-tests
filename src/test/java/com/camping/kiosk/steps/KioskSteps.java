package com.camping.kiosk.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.camping.common.support.ApiHelper;
import com.camping.common.support.CommonContext;
import io.cucumber.core.options.CurlOption.HttpMethod;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KioskSteps {

    public KioskSteps() {
        RestAssured.baseURI = CommonContext.KIOSK_BASE_URL;
    }

    @When("키오스크 컨테이너에 요청을 보낸다")
    public void 키오스크컨테이너에요청을보낸다() {
        CommonContext.lastResponse = ApiHelper.request(HttpMethod.GET, "/", null)
                .then().log().all()
                .extract().response();
    }

    @When("키오스크에서 전체 상품 조회를 요청한다")
    public void 키오스크에서전체상품조회를요청한다() {
        CommonContext.lastResponse = ApiHelper.request(HttpMethod.GET, "api/products", null);
    }

    @And("{int}개 이상의 상품 정보가 확인된다")
    public void 개이상의상품정보가확인된다(int count) {
        JsonPath jsonPath = CommonContext.lastResponse.then().log().all()
                .extract().response().jsonPath();
        List<Object> products = jsonPath.getList("$");
        assertThat(products.size()).isGreaterThanOrEqualTo(count);
    }

    @When("키오스크에서 결제 생성을 요청한다")
    public void 키오스크에서결제생성을요청한다() {
        // Create CartItem
        Map<String, Object> selectedProduct = (Map<String, Object>) CommonContext.lastParams.get("selectedProduct");
        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("productId", selectedProduct.get("id"));
        cartItem.put("productName", selectedProduct.get("name"));
        cartItem.put("unitPrice", selectedProduct.get("price"));
        cartItem.put("quantity", 1);
        List<Map<String, Object>> items = Collections.singletonList(cartItem);
        CommonContext.lastParams.put("items", items);

        // Create Payment Body
        Map<String, Object> body = new HashMap<>();
        body.put("items", items);
        body.put("paymentMethod", "CARD");

        CommonContext.lastResponse = ApiHelper.request(HttpMethod.POST, "api/payments", body);
        JsonPath paymentJsonPath = CommonContext.lastResponse.jsonPath();
        CommonContext.lastParams.put("paymentKey", paymentJsonPath.getString("paymentKey"));
        CommonContext.lastParams.put("orderId", paymentJsonPath.getString("orderId"));
        CommonContext.lastParams.put("amount", paymentJsonPath.getInt("amount"));
    }

    @When("키오스크에서 결제 승인을 요청한다")
    public void 키오스크에서결제승인을요청한다() {
        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", CommonContext.lastParams.get("paymentKey"));
        body.put("orderId", CommonContext.lastParams.get("orderId"));
        body.put("amount", CommonContext.lastParams.get("amount"));
        body.put("items", CommonContext.lastParams.get("items"));

        CommonContext.lastResponse = ApiHelper.request(HttpMethod.POST, "api/payments/confirm", body);
    }

    @Then("결제 승인 요청이 성공한다")
    public void 결제승인요청이성공한다() {
        assertThat(CommonContext.lastResponse.statusCode()).isEqualTo(200);
        JsonPath jsonPath = CommonContext.lastResponse.jsonPath();
        assertThat(jsonPath.getBoolean("success")).isTrue();
    }

    @Given("키오스크에서 전체 상품 목록을 확인한다")
    public void 키오스크에서전체상품목록을확인한다() {
        List<Map<String, Object>> products = ApiHelper.request(HttpMethod.GET, "api/products", null)
                .jsonPath().getList("$");
        CommonContext.lastParams.put("products", products);
    }

    @And("전체 상품 목록 중 구매할 상품을 선택한다")
    public void 전체상품목록중구매할상품을선택한다() {
        List<Map<String, Object>> products = (List<Map<String, Object>>) CommonContext.lastParams.get("products");
        Map<String, Object> selectedProduct = products.get(0);
        CommonContext.lastParams.put("selectedProduct", selectedProduct);
    }
}


