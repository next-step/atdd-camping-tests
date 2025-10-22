package com.camping.tests.steps;

import com.camping.tests.helpers.ContextHelper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObservabilitySteps {

    private static final String ADMIN_BASE_URL = System.getenv().getOrDefault("ADMIN_BASE_URL", "http://localhost:18082");
    private static final String PAYMENTS_BASE_URL = System.getenv().getOrDefault("PAYMENTS_BASE_URL", "http://localhost:18084");

    @And("관리자가 Admin 시스템에서 해당 예약을 조회할 수 있다")
    public void 관리자가_Admin_시스템에서_해당_예약을_조회할_수_있다() {
        String authToken = ContextHelper.get("authToken", String.class);
        Long reservationId = ContextHelper.get("reservationId", Long.class);

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(ADMIN_BASE_URL + "/admin/reservations");

        ContextHelper.set("adminReservationsResponse", response);

        assert response.statusCode() == 200 : "Expected 200, got " + response.statusCode();

        List<Map<String, Object>> reservations = response.jsonPath().getList("$");
        boolean found = reservations.stream()
                .anyMatch(r -> {
                    Object id = r.get("id");
                    if (id instanceof Integer) {
                        return ((Integer) id).longValue() == reservationId;
                    } else if (id instanceof Long) {
                        return id.equals(reservationId);
                    }
                    return false;
                });

        assert found : "Reservation " + reservationId + " should be found in admin system";
    }

    @And("관리자가 Admin 시스템에서 해당 예약을 조회한다")
    public void 관리자가_Admin_시스템에서_해당_예약을_조회한다() {
        관리자가_Admin_시스템에서_해당_예약을_조회할_수_있다();
    }

    @Then("Admin 시스템에서 예약 상태가 {string}이다")
    public void Admin_시스템에서_예약_상태_확인(String expectedStatus) {
        String authToken = ContextHelper.get("authToken", String.class);
        Long reservationId = ContextHelper.get("reservationId", Long.class);

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(ADMIN_BASE_URL + "/admin/reservations");

        assert response.statusCode() == 200 : "Expected 200, got " + response.statusCode();

        List<Map<String, Object>> reservations = response.jsonPath().getList("$");
        Map<String, Object> reservation = reservations.stream()
                .filter(r -> {
                    Object id = r.get("id");
                    if (id instanceof Integer) {
                        return ((Integer) id).longValue() == reservationId;
                    } else if (id instanceof Long) {
                        return id.equals(reservationId);
                    }
                    return false;
                })
                .findFirst()
                .orElse(null);

        assert reservation != null : "Reservation should be found";

        String actualStatus = (String) reservation.get("status");
        assert actualStatus.equals(expectedStatus) : "Expected status " + expectedStatus + ", got " + actualStatus;
    }

    @And("Admin 시스템에서 예약 상태가 {string} 또는 {string}이다")
    public void Admin_시스템에서_예약_상태가_여러_값_중_하나(String status1, String status2) {
        String authToken = ContextHelper.get("authToken", String.class);
        Long reservationId = ContextHelper.get("reservationId", Long.class);

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(ADMIN_BASE_URL + "/admin/reservations");

        assert response.statusCode() == 200 : "Expected 200, got " + response.statusCode();

        List<Map<String, Object>> reservations = response.jsonPath().getList("$");
        Map<String, Object> reservation = reservations.stream()
                .filter(r -> {
                    Object id = r.get("id");
                    if (id instanceof Integer) {
                        return ((Integer) id).longValue() == reservationId;
                    } else if (id instanceof Long) {
                        return id.equals(reservationId);
                    }
                    return false;
                })
                .findFirst()
                .orElse(null);

        assert reservation != null : "Reservation should be found";

        String actualStatus = (String) reservation.get("status");
        assert actualStatus.equals(status1) || actualStatus.equals(status2) :
                "Expected status " + status1 + " or " + status2 + ", got " + actualStatus;
    }

    @And("관리자가 해당 예약의 상태를 {string}로 변경한다")
    public void 관리자가_예약_상태를_변경한다(String newStatus) {
        String authToken = ContextHelper.get("authToken", String.class);
        Long reservationId = ContextHelper.get("reservationId", Long.class);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", newStatus);

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .contentType("application/json")
                .body(requestBody)
                .when()
                .patch(ADMIN_BASE_URL + "/admin/reservations/" + reservationId + "/status");

        ContextHelper.set("adminUpdateResponse", response);

        assert response.statusCode() == 200 : "Expected 200, got " + response.statusCode();
    }

    @And("관리자가 Admin 시스템에서 전체 예약 목록을 조회한다")
    public void 관리자가_전체_예약_목록을_조회한다() {
        String authToken = ContextHelper.get("authToken", String.class);

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(ADMIN_BASE_URL + "/admin/reservations");

        ContextHelper.set("adminReservationsListResponse", response);

        assert response.statusCode() == 200 : "Expected 200, got " + response.statusCode();
    }

    @Then("예약 목록에 최소 {int}개의 예약이 포함되어 있다")
    public void 예약_목록_개수_확인(int minCount) {
        Response response = ContextHelper.get("adminReservationsListResponse", Response.class);

        List<Map<String, Object>> reservations = response.jsonPath().getList("$");
        assert reservations.size() >= minCount : "Expected at least " + minCount + " reservations, got " + reservations.size();
    }

    @And("관리자가 {string}의 예약을 조회한다")
    public void 관리자가_특정_고객의_예약을_조회한다(String customerName) {
        String authToken = ContextHelper.get("authToken", String.class);

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(ADMIN_BASE_URL + "/admin/reservations");

        ContextHelper.set("adminCustomerReservationsResponse", response);

        assert response.statusCode() == 200 : "Expected 200, got " + response.statusCode();

        List<Map<String, Object>> reservations = response.jsonPath().getList("$");
        List<Map<String, Object>> filteredReservations = reservations.stream()
                .filter(r -> {
                    String name = (String) r.get("customerName");
                    return name != null && name.contains(customerName);
                })
                .collect(java.util.stream.Collectors.toList());

        ContextHelper.set("filteredReservations", filteredReservations);
    }

    @Then("조회된 예약의 고객명이 {string}이다")
    public void 조회된_예약의_고객명_확인(String expectedNamePart) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> reservations = ContextHelper.get("filteredReservations", List.class);

        assert !reservations.isEmpty() : "Should find at least one reservation";

        String customerName = (String) reservations.get(0).get("customerName");
        assert customerName.contains(expectedNamePart) : "Customer name should contain " + expectedNamePart;
    }

    @And("관리자가 {string} 상태의 예약을 조회한다")
    public void 관리자가_특정_상태의_예약을_조회한다(String status) {
        String authToken = ContextHelper.get("authToken", String.class);

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(ADMIN_BASE_URL + "/admin/reservations");

        assert response.statusCode() == 200 : "Expected 200, got " + response.statusCode();

        // Filter by status
        List<Map<String, Object>> reservations = response.jsonPath().getList("$");
        List<Map<String, Object>> filteredReservations = reservations.stream()
                .filter(r -> status.equals(r.get("status")))
                .collect(java.util.stream.Collectors.toList());

        ContextHelper.set("statusFilteredReservations", filteredReservations);
    }

    @Then("조회된 예약 목록에 취소된 예약이 포함되어 있다")
    public void 조회된_예약_목록에_취소된_예약이_포함되어_있다() {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> reservations = ContextHelper.get("statusFilteredReservations", List.class);

        assert !reservations.isEmpty() : "Should find at least one cancelled reservation";

        boolean hasCancelled = reservations.stream()
                .anyMatch(r -> "CANCELLED".equals(r.get("status")));

        assert hasCancelled : "Should have at least one CANCELLED reservation";
    }

    @Given("인증 없이 Admin 시스템에 접근하려고 한다")
    public void 인증_없이_Admin_시스템에_접근하려고_한다() {
        Response response = RestAssured.given()
                .when()
                .get(ADMIN_BASE_URL + "/admin/reservations");

        ContextHelper.set("unauthorizedResponse", response);
    }

    @Then("{int} 인증 오류가 발생한다")
    public void 인증_오류_발생(int expectedStatusCode) {
        Response response = ContextHelper.get("unauthorizedResponse", Response.class);
        assert response.statusCode() == expectedStatusCode : "Expected " + expectedStatusCode + ", got " + response.statusCode();
    }

    @And("WireMock에 결제 요청 기록이 남는다")
    public void WireMock에_결제_요청_기록이_남는다() {
        Response response = RestAssured.given()
                .when()
                .get(PAYMENTS_BASE_URL + "/__admin/requests");

        assert response.statusCode() == 200 : "WireMock admin API should be accessible";

        List<Map<String, Object>> requests = response.jsonPath().getList("requests");
        assert !requests.isEmpty() : "Should have at least one payment request recorded";

        ContextHelper.set("wiremockRequests", requests);
    }

    @And("WireMock 호출 기록에 결제 확정 요청이 있다")
    public void WireMock_호출_기록에_결제_확정_요청이_있다() {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> requests = ContextHelper.get("wiremockRequests", List.class);

        if (requests == null) {
            WireMock에_결제_요청_기록이_남는다();
            requests = ContextHelper.get("wiremockRequests", List.class);
        }

        boolean hasConfirmRequest = requests.stream()
                .anyMatch(r -> {
                    Map<String, Object> request = (Map<String, Object>) r.get("request");
                    String url = (String) request.get("url");
                    return url != null && url.contains("/payments/confirm");
                });

        assert hasConfirmRequest : "Should have payment confirm request in WireMock";
    }
}
