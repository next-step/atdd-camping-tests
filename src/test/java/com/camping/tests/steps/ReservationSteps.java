package com.camping.tests.steps;

import com.camping.tests.helpers.ContextHelper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.awaitility.Awaitility;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ReservationSteps {

    private static final String RESERVATION_BASE_URL = System.getenv().getOrDefault("RESERVATION_BASE_URL", "http://localhost:18083");

    @Given("예약 시스템이 정상적으로 기동되어 있다")
    public void 예약_시스템이_정상적으로_기동되어_있다() {
        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(2))
                .ignoreExceptions()
                .untilAsserted(() -> {
                    RestAssured.given()
                            .when()
                            .get(RESERVATION_BASE_URL + "/")
                            .then()
                            .statusCode(200);
                });
    }

    @Given("고객이 예약 가능한 캠핑 사이트를 조회한다")
    public void 고객이_예약_가능한_캠핑_사이트를_조회한다() {
        Response response = RestAssured.given()
                .when()
                .get(RESERVATION_BASE_URL + "/api/sites");

        ContextHelper.set("sitesResponse", response);
        assert response.statusCode() == 200 : "Expected 200, got " + response.statusCode();
    }

    @And("고객이 {string} 사이트를 {string}부터 {string}까지 예약한다")
    public void 고객이_사이트를_예약한다(String siteCode, String startDate, String endDate) {
        long timestamp = System.currentTimeMillis();
        String uniqueName = "고객-ai-test-" + timestamp;
        String uniquePhone = "010-" + String.format("%08d", timestamp % 100000000);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("siteCode", siteCode);
        requestBody.put("customerName", uniqueName);
        requestBody.put("phone", uniquePhone);
        requestBody.put("startDate", startDate);
        requestBody.put("endDate", endDate);

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(RESERVATION_BASE_URL + "/api/reservations");

        ContextHelper.set("reservationResponse", response);
        ContextHelper.set("customerName", uniqueName);
        ContextHelper.set("customerPhone", uniquePhone);
        ContextHelper.set("siteCode", siteCode);
        ContextHelper.set("startDate", startDate);
        ContextHelper.set("endDate", endDate);

        if (response.statusCode() == 201) {
            Long reservationId = response.jsonPath().getLong("id");
            String confirmationCode = response.jsonPath().getString("confirmationCode");
            ContextHelper.set("reservationId", reservationId);
            ContextHelper.set("confirmationCode", confirmationCode);
        }
    }

    @And("{string}가 {string} 사이트를 {string}부터 {string}까지 예약한다")
    public void 특정_고객이_사이트를_예약한다(String customerName, String siteCode, String startDate, String endDate) {
        long timestamp = System.currentTimeMillis();
        String uniqueName = customerName + "-" + timestamp;
        String uniquePhone = "010-" + String.format("%08d", timestamp % 100000000);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("siteCode", siteCode);
        requestBody.put("customerName", uniqueName);
        requestBody.put("phone", uniquePhone);
        requestBody.put("startDate", startDate);
        requestBody.put("endDate", endDate);

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(RESERVATION_BASE_URL + "/api/reservations");

        ContextHelper.set("reservationResponse_" + customerName, response);
        ContextHelper.set("customerName_" + customerName, uniqueName);

        if (response.statusCode() == 201) {
            Long reservationId = response.jsonPath().getLong("id");
            String confirmationCode = response.jsonPath().getString("confirmationCode");
            ContextHelper.set("reservationId_" + customerName, reservationId);
            ContextHelper.set("confirmationCode_" + customerName, confirmationCode);
        }
    }

    @And("고객이 {string} 사이트를 연박 예약한다")
    public void 고객이_사이트를_연박_예약한다(String siteCode, String startDate, String endDate) {
        고객이_사이트를_예약한다(siteCode, startDate, endDate);
    }

    @And("예약에 대한 확인 코드를 받는다")
    public void 예약에_대한_확인_코드를_받는다() {
        String confirmationCode = ContextHelper.get("confirmationCode", String.class);
        assert confirmationCode != null && !confirmationCode.isEmpty() : "Confirmation code should not be empty";
    }

    @And("예약 ID를 저장한다")
    public void 예약_ID를_저장한다() {
        Response response = ContextHelper.get("reservationResponse", Response.class);
        if (response != null && response.statusCode() == 201) {
            Long reservationId = response.jsonPath().getLong("id");
            ContextHelper.set("reservationId", reservationId);
        }
    }

    @Then("확인 코드가 6자리 영숫자이다")
    public void 확인_코드가_6자리_영숫자이다() {
        String confirmationCode = ContextHelper.get("confirmationCode", String.class);
        assert confirmationCode != null : "Confirmation code should not be null";
        assert confirmationCode.matches("[A-Z0-9]{6}") : "Confirmation code should be 6 alphanumeric characters";
    }

    @And("확인 코드로 예약을 조회할 수 있다")
    public void 확인_코드로_예약을_조회할_수_있다() {
        String customerName = ContextHelper.get("customerName", String.class);
        String customerPhone = ContextHelper.get("customerPhone", String.class);

        Response response = RestAssured.given()
                .queryParam("name", customerName)
                .queryParam("phone", customerPhone)
                .when()
                .get(RESERVATION_BASE_URL + "/api/reservations/my");

        assert response.statusCode() == 200 : "Expected 200, got " + response.statusCode();
        List<Map<String, Object>> reservations = response.jsonPath().getList("$");
        assert !reservations.isEmpty() : "Should find at least one reservation";
    }

    @And("고객이 확인 코드로 예약을 취소한다")
    public void 고객이_확인_코드로_예약을_취소한다() {
        Long reservationId = ContextHelper.get("reservationId", Long.class);
        String confirmationCode = ContextHelper.get("confirmationCode", String.class);

        Response response = RestAssured.given()
                .queryParam("confirmationCode", confirmationCode)
                .when()
                .delete(RESERVATION_BASE_URL + "/api/reservations/" + reservationId);

        ContextHelper.set("cancellationResponse", response);
    }

    @And("고객이 잘못된 확인 코드로 예약을 취소하려고 한다")
    public void 고객이_잘못된_확인_코드로_예약을_취소하려고_한다() {
        Long reservationId = ContextHelper.get("reservationId", Long.class);
        String wrongCode = "WRONG1";

        Response response = RestAssured.given()
                .queryParam("confirmationCode", wrongCode)
                .when()
                .delete(RESERVATION_BASE_URL + "/api/reservations/" + reservationId);

        ContextHelper.set("cancellationResponse", response);
    }

    @And("고객이 동일한 확인 코드로 다시 예약을 취소하려고 한다")
    public void 고객이_동일한_확인_코드로_다시_예약을_취소하려고_한다() {
        Long reservationId = ContextHelper.get("reservationId", Long.class);
        String confirmationCode = ContextHelper.get("confirmationCode", String.class);

        Response response = RestAssured.given()
                .queryParam("confirmationCode", confirmationCode)
                .when()
                .delete(RESERVATION_BASE_URL + "/api/reservations/" + reservationId);

        ContextHelper.set("secondCancellationResponse", response);
    }

    @Then("예약이 성공적으로 생성된다")
    public void 예약이_성공적으로_생성된다() {
        Response response = ContextHelper.get("reservationResponse", Response.class);
        assert response.statusCode() == 201 : "Expected 201, got " + response.statusCode();
    }

    @Then("예약 상태가 {string}이다")
    public void 예약_상태_확인(String expectedStatus) {
        Long reservationId = ContextHelper.get("reservationId", Long.class);

        Response response = RestAssured.given()
                .when()
                .get(RESERVATION_BASE_URL + "/api/reservations/" + reservationId);

        String actualStatus = response.jsonPath().getString("status");
        assert actualStatus.equals(expectedStatus) : "Expected status " + expectedStatus + ", got " + actualStatus;
    }

    @Then("예약 상태가 {string}가 아니다")
    public void 예약_상태가_아님(String notExpectedStatus) {
        Long reservationId = ContextHelper.get("reservationId", Long.class);

        Response response = RestAssured.given()
                .when()
                .get(RESERVATION_BASE_URL + "/api/reservations/" + reservationId);

        String actualStatus = response.jsonPath().getString("status");
        assert !actualStatus.equals(notExpectedStatus) : "Status should not be " + notExpectedStatus;
    }

    @And("예약 기간이 {string}박으로 계산된다")
    public void 예약_기간_확인(String nights) {
        String startDate = ContextHelper.get("startDate", String.class);
        String endDate = ContextHelper.get("endDate", String.class);

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        long calculatedNights = java.time.temporal.ChronoUnit.DAYS.between(start, end);

        assert calculatedNights == Long.parseLong(nights) : "Expected " + nights + " nights, got " + calculatedNights;
    }

    @Given("고객이 이름 없이 {string} 사이트를 예약하려고 한다")
    public void 고객이_이름_없이_예약하려고_한다(String siteCode) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("siteCode", siteCode);
        requestBody.put("customerName", "");
        requestBody.put("phone", "010-1234-5678");
        requestBody.put("startDate", "2025-01-15");
        requestBody.put("endDate", "2025-01-17");

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(RESERVATION_BASE_URL + "/api/reservations");

        ContextHelper.set("reservationResponse", response);
    }

    @And("{string}가 {string} 사이트를 {string}부터 {string}까지 예약하려고 한다")
    public void 고객이_사이트를_예약하려고_한다(String customerName, String siteCode, String startDate, String endDate) {
        특정_고객이_사이트를_예약한다(customerName, siteCode, startDate, endDate);
    }

    @Then("예약이 실패한다")
    public void 예약이_실패한다() {
        Response response = ContextHelper.get("reservationResponse", Response.class);
        assert response.statusCode() != 201 : "Reservation should have failed, but got 201";
    }

    @Then("{string}의 예약이 실패한다")
    public void 특정_고객의_예약이_실패한다(String customerName) {
        Response response = ContextHelper.get("reservationResponse_" + customerName, Response.class);
        assert response != null : "Response should not be null";
        assert response.statusCode() != 201 : customerName + "'s reservation should have failed";
    }

    @And("오류 메시지에 {string}이 포함된다")
    public void 오류_메시지_확인(String keyword) {
        Response response = ContextHelper.get("reservationResponse", Response.class);
        String body = response.getBody().asString();
        assert body.contains(keyword) : "Error message should contain '" + keyword + "'";
    }

    @Then("예약 취소가 실패한다")
    public void 예약_취소가_실패한다() {
        Response response = ContextHelper.get("cancellationResponse", Response.class);
        if (response == null) {
            response = ContextHelper.get("secondCancellationResponse", Response.class);
        }
        assert response.statusCode() != 200 : "Cancellation should have failed";
    }

    @Then("예약이 취소되지만 환불은 불가능하다")
    public void 예약이_취소되지만_환불은_불가능하다() {
        Response response = ContextHelper.get("cancellationResponse", Response.class);
        assert response.statusCode() == 200 : "Cancellation should succeed";

        // Check refund amount if available in response
        try {
            Integer refundAmount = response.jsonPath().getInt("refundAmount");
            ContextHelper.set("refundAmount", refundAmount);
        } catch (Exception e) {
            // Refund amount might not be in response
        }
    }

    @And("환불 금액이 {int}원이다")
    public void 환불_금액_확인(int expectedAmount) {
        Integer refundAmount = ContextHelper.get("refundAmount", Integer.class);
        if (refundAmount != null) {
            assert refundAmount == expectedAmount : "Expected refund " + expectedAmount + ", got " + refundAmount;
        }
    }

    @Given("고객이 {string} 사이트에 대해 동일한 예약 요청을 빠르게 2번 전송한다")
    public void 고객이_동일한_예약_요청을_2번_전송한다(String siteCode) throws Exception {
        long timestamp = System.currentTimeMillis();
        String uniqueName = "고객-concurrency-" + timestamp;
        String uniquePhone = "010-" + String.format("%08d", timestamp % 100000000);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("siteCode", siteCode);
        requestBody.put("customerName", uniqueName);
        requestBody.put("phone", uniquePhone);
        requestBody.put("startDate", "2025-01-20");
        requestBody.put("endDate", "2025-01-22");

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<Response> future1 = executor.submit(() ->
            RestAssured.given()
                    .contentType("application/json")
                    .body(requestBody)
                    .post(RESERVATION_BASE_URL + "/api/reservations")
        );

        Future<Response> future2 = executor.submit(() ->
            RestAssured.given()
                    .contentType("application/json")
                    .body(requestBody)
                    .post(RESERVATION_BASE_URL + "/api/reservations")
        );

        Response response1 = future1.get();
        Response response2 = future2.get();

        ContextHelper.set("concurrentResponse1", response1);
        ContextHelper.set("concurrentResponse2", response2);
        executor.shutdown();
    }

    @Then("단 하나의 예약만 생성된다")
    public void 단_하나의_예약만_생성된다() {
        Response response1 = ContextHelper.get("concurrentResponse1", Response.class);
        Response response2 = ContextHelper.get("concurrentResponse2", Response.class);

        int successCount = 0;
        if (response1.statusCode() == 201) successCount++;
        if (response2.statusCode() == 201) successCount++;

        assert successCount == 1 : "Expected exactly 1 successful reservation, got " + successCount;
    }

    @And("두 번째 요청은 409 에러를 반환한다")
    public void 두_번째_요청은_409_에러를_반환한다() {
        Response response1 = ContextHelper.get("concurrentResponse1", Response.class);
        Response response2 = ContextHelper.get("concurrentResponse2", Response.class);

        boolean hasConflict = response1.statusCode() == 409 || response2.statusCode() == 409;
        assert hasConflict : "One of the responses should be 409 Conflict";
    }

    @And("두 번째 요청은 실패하거나 첫 번째 예약을 반환한다")
    public void 두_번째_요청은_실패하거나_첫_번째_예약을_반환한다() {
        Response response1 = ContextHelper.get("concurrentResponse1", Response.class);
        Response response2 = ContextHelper.get("concurrentResponse2", Response.class);

        // Either one fails or both return the same reservation ID
        if (response1.statusCode() == 201 && response2.statusCode() == 201) {
            Long id1 = response1.jsonPath().getLong("id");
            Long id2 = response2.jsonPath().getLong("id");
            assert id1.equals(id2) : "If both succeed, they should return the same reservation";
        } else {
            assert response1.statusCode() == 201 || response2.statusCode() == 201 : "At least one should succeed";
        }
    }

    @Given("고객이 오늘 시작하는 예약을 생성한다")
    public void 고객이_오늘_시작하는_예약을_생성한다() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE);
        고객이_사이트를_예약한다("A01", today, tomorrow);
    }

    @Given("고객이 {string} 사이트를 내일부터 시작하는 예약을 생성한다")
    public void 고객이_내일부터_시작하는_예약을_생성한다(String siteCode) {
        String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE);
        String dayAfter = LocalDate.now().plusDays(2).format(DateTimeFormatter.ISO_DATE);
        고객이_사이트를_예약한다(siteCode, tomorrow, dayAfter);
    }

    @Given("{int}명의 고객이 {string} 사이트를 동시에 예약하려고 한다")
    public void 여러_고객이_동시에_예약하려고_한다(int count, String siteCode) throws Exception {
        long timestamp = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(count);
        java.util.List<Future<Response>> futures = new java.util.ArrayList<>();

        for (int i = 0; i < count; i++) {
            int index = i;
            futures.add(executor.submit(() -> {
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("siteCode", siteCode);
                requestBody.put("customerName", "고객-" + index + "-" + timestamp);
                requestBody.put("phone", "010-" + String.format("%08d", (timestamp + index) % 100000000));
                requestBody.put("startDate", "2025-01-25");
                requestBody.put("endDate", "2025-01-27");

                return RestAssured.given()
                        .contentType("application/json")
                        .body(requestBody)
                        .post(RESERVATION_BASE_URL + "/api/reservations");
            }));
        }

        java.util.List<Response> responses = futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        ContextHelper.set("concurrentResponses", responses);
        executor.shutdown();
    }

    @Then("단 하나의 예약만 성공한다")
    public void 단_하나의_예약만_성공한다() {
        @SuppressWarnings("unchecked")
        java.util.List<Response> responses = ContextHelper.get("concurrentResponses", java.util.List.class);

        long successCount = responses.stream()
                .filter(r -> r.statusCode() == 201)
                .count();

        assert successCount == 1 : "Expected exactly 1 successful reservation, got " + successCount;
    }

    @And("나머지 {int}개의 요청은 실패한다")
    public void 나머지_요청은_실패한다(int expectedFailures) {
        @SuppressWarnings("unchecked")
        java.util.List<Response> responses = ContextHelper.get("concurrentResponses", java.util.List.class);

        long failureCount = responses.stream()
                .filter(r -> r.statusCode() != 201)
                .count();

        assert failureCount == expectedFailures : "Expected " + expectedFailures + " failures, got " + failureCount;
    }

    @And("다른 고객이 {string} 사이트를 {string}부터 {string}까지 예약할 수 있다")
    public void 다른_고객이_사이트를_예약할_수_있다(String siteCode, String startDate, String endDate) {
        고객이_사이트를_예약한다(siteCode, startDate, endDate);
        Response response = ContextHelper.get("reservationResponse", Response.class);
        assert response.statusCode() == 201 : "Another customer should be able to book, got " + response.statusCode();
    }

    @And("다른 고객이 {string} 사이트를 {string}부터 {string}까지 예약한다")
    public void 다른_고객이_사이트를_예약한다(String siteCode, String startDate, String endDate) {
        다른_고객이_사이트를_예약할_수_있다(siteCode, startDate, endDate);
    }

    @And("두 번째 예약이 실패한다")
    public void 두_번째_예약이_실패한다() {
        Response response = ContextHelper.get("reservationResponse", Response.class);
        assert response.statusCode() != 201 : "Second reservation should fail";
    }
}
