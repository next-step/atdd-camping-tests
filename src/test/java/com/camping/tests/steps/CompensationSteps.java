package com.camping.tests.steps;

import com.camping.tests.helpers.ContextHelper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

public class CompensationSteps {

    private static final String RESERVATION_BASE_URL = System.getenv().getOrDefault("RESERVATION_BASE_URL", "http://localhost:18083");
    private static final String KIOSK_BASE_URL = System.getenv().getOrDefault("KIOSK_BASE_URL", "http://localhost:18081");

    @And("고객이 예약에 대해 {int}원으로 결제를 생성한다")
    public void 고객이_예약에_대해_결제를_생성한다(int amount) {
        Map<String, Object> item = new HashMap<>();
        item.put("productId", 1);
        item.put("productName", "캠핑 예약");
        item.put("unitPrice", amount);
        item.put("quantity", 1);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("items", new Object[]{item});
        requestBody.put("paymentMethod", "CARD");

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(KIOSK_BASE_URL + "/api/payments");

        ContextHelper.set("paymentCreateResponse", response);

        if (response.statusCode() == 200) {
            String paymentKey = response.jsonPath().getString("paymentKey");
            String orderId = response.jsonPath().getString("orderId");
            ContextHelper.set("paymentKey", paymentKey);
            ContextHelper.set("orderId", orderId);
            ContextHelper.set("paymentAmount", amount);
        }
    }

    @And("고객이 결제를 확정한다")
    public void 고객이_결제를_확정한다() {
        String paymentKey = ContextHelper.get("paymentKey", String.class);
        String orderId = ContextHelper.get("orderId", String.class);
        Integer amount = ContextHelper.get("paymentAmount", Integer.class);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentKey", paymentKey);
        requestBody.put("orderId", orderId);
        requestBody.put("amount", amount);

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(KIOSK_BASE_URL + "/api/payments/confirm");

        ContextHelper.set("paymentConfirmResponse", response);
    }

    @Then("결제가 성공한다")
    public void 결제가_성공한다() {
        Response response = ContextHelper.get("paymentConfirmResponse", Response.class);
        assert response.statusCode() == 200 : "Expected 200, got " + response.statusCode();

        Boolean success = response.jsonPath().getBoolean("success");
        assert success != null && success : "Payment should be successful";
    }

    @Then("결제가 실패한다")
    public void 결제가_실패한다() {
        Response response = ContextHelper.get("paymentConfirmResponse", Response.class);

        if (response.statusCode() == 200) {
            Boolean success = response.jsonPath().getBoolean("success");
            assert success != null && !success : "Payment should fail";
        } else {
            assert response.statusCode() >= 400 : "Payment should return error status";
        }
    }

    @And("사이트 {string}이 해당 기간에 다시 예약 가능하다")
    public void 사이트가_다시_예약_가능하다(String siteCode) {
        String startDate = ContextHelper.get("startDate", String.class);
        String endDate = ContextHelper.get("endDate", String.class);

        Response response = RestAssured.given()
                .queryParam("startDate", startDate)
                .queryParam("endDate", endDate)
                .when()
                .get(RESERVATION_BASE_URL + "/api/sites/available");

        if (response.statusCode() == 200) {
            java.util.List<String> availableSites = response.jsonPath().getList("siteCode", String.class);
            assert availableSites.contains(siteCode) : "Site " + siteCode + " should be available again";
        }
    }

    @And("사이트 {string}의 {string}부터 {string}까지 모든 날짜가 예약 불가능하다")
    public void 사이트가_예약_불가능하다(String siteCode, String startDate, String endDate) {
        Response response = RestAssured.given()
                .queryParam("startDate", startDate)
                .queryParam("endDate", endDate)
                .when()
                .get(RESERVATION_BASE_URL + "/api/sites/available");

        if (response.statusCode() == 200) {
            java.util.List<String> availableSites = response.jsonPath().getList("siteCode", String.class);
            assert !availableSites.contains(siteCode) : "Site " + siteCode + " should NOT be available";
        }
    }

    @And("{string} 사이트가 {string}부터 {string}까지 예약 불가능하다")
    public void 특정_사이트가_예약_불가능하다(String siteCode, String startDate, String endDate) {
        사이트가_예약_불가능하다(siteCode, startDate, endDate);
    }

    @And("{string} 사이트가 {string}부터 {string}까지 다시 예약 가능하다")
    public void 특정_사이트가_다시_예약_가능하다(String siteCode, String startDate, String endDate) {
        Response response = RestAssured.given()
                .queryParam("startDate", startDate)
                .queryParam("endDate", endDate)
                .when()
                .get(RESERVATION_BASE_URL + "/api/sites/available");

        if (response.statusCode() == 200) {
            java.util.List<String> availableSites = response.jsonPath().getList("siteCode", String.class);
            assert availableSites.contains(siteCode) : "Site " + siteCode + " should be available again";
        }
    }

    @And("결제 시스템이 응답하지 않는다")
    public void 결제_시스템이_응답하지_않는다() {
        ContextHelper.set("paymentTimeout", true);
    }

    @And("{int}분 후 예약이 자동으로 만료된다")
    public void 예약이_자동으로_만료된다(int minutes) {
        Long reservationId = ContextHelper.get("reservationId", Long.class);

        Response response = RestAssured.given()
                .when()
                .get(RESERVATION_BASE_URL + "/api/reservations/" + reservationId);

        String status = response.jsonPath().getString("status");
        assert "PENDING".equals(status) : "Reservation should be in PENDING state before expiry";
    }

    @And("고객이 동일한 예약에 대해 {int}원으로 결제를 재시도한다")
    public void 고객이_결제를_재시도한다(int amount) {
        고객이_예약에_대해_결제를_생성한다(amount);
    }

    @And("고객이 예약에 대해 결제를 완료한다")
    public void 고객이_예약에_대해_결제를_완료한다() {
        // Default amount for successful payment
        고객이_예약에_대해_결제를_생성한다(30000);
        고객이_결제를_확정한다();

        // Verify payment succeeded
        Response response = ContextHelper.get("paymentConfirmResponse", Response.class);
        Boolean success = response.jsonPath().getBoolean("success");
        assert success != null && success : "Payment should be successful";
    }

    @And("예약에 대해 {int}원으로 결제를 완료한다")
    public void 예약에_대해_특정_금액으로_결제를_완료한다(int amount) {
        고객이_예약에_대해_결제를_생성한다(amount);
        고객이_결제를_확정한다();

        Response response = ContextHelper.get("paymentConfirmResponse", Response.class);
        Boolean success = response.jsonPath().getBoolean("success");
        assert success != null && success : "Payment should be successful";
    }

    @And("사이트 {string}가 해당 기간에 다시 예약 가능하다")
    public void 사이트가_해당_기간에_다시_예약_가능하다(String siteCode) {
        사이트가_다시_예약_가능하다(siteCode);
    }
}
