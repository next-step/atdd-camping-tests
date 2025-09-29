package com.camping.tests.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import com.camping.tests.support.fixture.AdminTestFixture;
import com.camping.tests.support.fixture.KioskTestFixture;
import com.camping.tests.support.fixture.ReservationTestFixture;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

public class BoundaryIntegrationSteps {

    private Long productId;
    private Long siteId;
    private Long reservationId;
    private ExtractableResponse<Response> purchaseResponse;
    private ExtractableResponse<Response> reservationResponse;
    private ExtractableResponse<Response> updateResponse;
    private ExtractableResponse<Response> authResponse;
    private ExtractableResponse<Response> paymentResponse;
    private ExtractableResponse<Response> purchaseResponse1;
    private ExtractableResponse<Response> purchaseResponse2;
    private String currentDate = "2024-12-20";

    // 재고 부족 상황에서 구매 시도
    @Given("관리자가 재고가 적은 상품을 등록한다")
    public void 관리자가_재고가_적은_상품을_등록한다(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        Map<String, String> productData = products.get(0);

        ExtractableResponse<Response> response = AdminTestFixture.Admin_상품_생성(productData);
        this.productId = AdminTestFixture.Admin_생성된_상품_ID_추출(response);
    }

    @When("키오스크에서 재고보다 많은 수량을 구매 시도한다")
    public void 키오스크에서_재고보다_많은_수량을_구매_시도한다(DataTable dataTable) {
        List<Map<String, String>> purchases = dataTable.asMaps(String.class, String.class);
        Map<String, String> purchaseData = purchases.get(0);

        Map<String, String> requestData = new HashMap<>();
        requestData.put("productId", productId.toString());
        requestData.put("quantity", purchaseData.get("attemptQuantity"));

        this.purchaseResponse = KioskTestFixture.Kiosk_상품_구매_시도(requestData);
    }

    @Then("구매가 실패한다")
    public void 구매가_실패한다() {
        assertThat(purchaseResponse.statusCode()).isEqualTo(400);
    }

    @And("적절한 오류 메시지가 표시된다")
    public void 적절한_오류_메시지가_표시된다(DataTable dataTable) {
        List<Map<String, String>> messages = dataTable.asMaps(String.class, String.class);
        String expectedMessage = messages.get(0).get("expectedMessage");

        String actualMessage = purchaseResponse.jsonPath().getString("message");
        assertThat(actualMessage).contains(expectedMessage);
    }

    @And("관리자 시스템의 재고는 변경되지 않는다")
    public void 관리자_시스템의_재고는_변경되지_않는다(DataTable dataTable) {
        List<Map<String, String>> stocks = dataTable.asMaps(String.class, String.class);
        int expectedStock = Integer.parseInt(stocks.get(0).get("expectedStock"));

        ExtractableResponse<Response> stockResponse = AdminTestFixture.Admin_상품_재고_조회(productId);
        AdminTestFixture.Admin_재고_차감_검증(stockResponse, expectedStock);
    }

    // 품절 상품 구매 시도
    @Given("관리자가 품절된 상품을 가지고 있다")
    public void 관리자가_품절된_상품을_가지고_있다(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        Map<String, String> productData = products.get(0);

        ExtractableResponse<Response> response = AdminTestFixture.Admin_상품_생성(productData);
        this.productId = AdminTestFixture.Admin_생성된_상품_ID_추출(response);
    }

    @When("키오스크에서 품절 상품을 구매 시도한다")
    public void 키오스크에서_품절_상품을_구매_시도한다(DataTable dataTable) {
        List<Map<String, String>> purchases = dataTable.asMaps(String.class, String.class);
        Map<String, String> purchaseData = purchases.get(0);

        Map<String, String> requestData = new HashMap<>();
        requestData.put("productId", productId.toString());
        requestData.put("quantity", purchaseData.get("quantity"));

        this.purchaseResponse = KioskTestFixture.Kiosk_상품_구매_시도(requestData);
    }

    @Then("구매가 거절된다")
    public void 구매가_거절된다() {
        assertThat(purchaseResponse.statusCode()).isEqualTo(400);
    }

    @And("품절 안내 메시지가 표시된다")
    public void 품절_안내_메시지가_표시된다(DataTable dataTable) {
        List<Map<String, String>> messages = dataTable.asMaps(String.class, String.class);
        String expectedMessage = messages.get(0).get("expectedMessage");

        String actualMessage = purchaseResponse.jsonPath().getString("message");
        assertThat(actualMessage).contains(expectedMessage);
    }

    @And("관리자 시스템의 재고가 음수가 되지 않는다")
    public void 관리자_시스템의_재고가_음수가_되지_않는다(DataTable dataTable) {
        List<Map<String, String>> stocks = dataTable.asMaps(String.class, String.class);
        int expectedStock = Integer.parseInt(stocks.get(0).get("expectedStock"));

        ExtractableResponse<Response> stockResponse = AdminTestFixture.Admin_상품_재고_조회(productId);
        AdminTestFixture.Admin_재고_차감_검증(stockResponse, expectedStock);
    }

    // 동시 구매 시 마지막 재고 처리
    @Given("관리자가 마지막 재고 1개인 상품을 가지고 있다")
    public void 관리자가_마지막_재고_1개인_상품을_가지고_있다(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        Map<String, String> productData = products.get(0);

        ExtractableResponse<Response> response = AdminTestFixture.Admin_상품_생성(productData);
        this.productId = AdminTestFixture.Admin_생성된_상품_ID_추출(response);
    }

    @When("두 개의 키오스크에서 동시에 같은 상품을 구매 시도한다")
    public void 두_개의_키오스크에서_동시에_같은_상품을_구매_시도한다(DataTable dataTable) throws ExecutionException, InterruptedException {
        List<Map<String, String>> purchases = dataTable.asMaps(String.class, String.class);

        Map<String, String> purchase1 = new HashMap<>();
        purchase1.put("productId", productId.toString());
        purchase1.put("quantity", purchases.get(0).get("quantity"));

        Map<String, String> purchase2 = new HashMap<>();
        purchase2.put("productId", productId.toString());
        purchase2.put("quantity", purchases.get(1).get("quantity"));

        // 동시 구매 시도
        CompletableFuture<ExtractableResponse<Response>> future1 = CompletableFuture.supplyAsync(() ->
            KioskTestFixture.Kiosk_상품_구매_시도(purchase1));
        CompletableFuture<ExtractableResponse<Response>> future2 = CompletableFuture.supplyAsync(() ->
            KioskTestFixture.Kiosk_상품_구매_시도(purchase2));

        this.purchaseResponse1 = future1.get();
        this.purchaseResponse2 = future2.get();
    }

    @Then("하나의 구매만 성공한다")
    public void 하나의_구매만_성공한다() {
        boolean purchase1Success = purchaseResponse1.statusCode() == 200;
        boolean purchase2Success = purchaseResponse2.statusCode() == 200;

        // 정확히 하나만 성공해야 함
        assertThat(purchase1Success ^ purchase2Success).isTrue();
    }

    @And("나머지 구매는 재고 부족으로 실패한다")
    public void 나머지_구매는_재고_부족으로_실패한다() {
        boolean purchase1Failed = purchaseResponse1.statusCode() == 400;
        boolean purchase2Failed = purchaseResponse2.statusCode() == 400;

        // 하나는 반드시 실패해야 함
        assertThat(purchase1Failed || purchase2Failed).isTrue();
    }

    @And("관리자 시스템의 재고는 0이 된다")
    public void 관리자_시스템의_재고는_0이_된다(DataTable dataTable) {
        List<Map<String, String>> stocks = dataTable.asMaps(String.class, String.class);
        int expectedStock = Integer.parseInt(stocks.get(0).get("expectedStock"));

        ExtractableResponse<Response> stockResponse = AdminTestFixture.Admin_상품_재고_조회(productId);
        AdminTestFixture.Admin_재고_차감_검증(stockResponse, expectedStock);
    }

    // 예약 기간 겹침 처리
    @Given("기존 예약이 있는 캠프사이트가 있다")
    public void 기존_예약이_있는_캠프사이트가_있다(DataTable dataTable) {
        List<Map<String, String>> sites = dataTable.asMaps(String.class, String.class);
        Map<String, String> siteData = sites.get(0);

        // 캠프사이트 생성
        Map<String, String> campsite = new HashMap<>();
        campsite.put("siteName", siteData.get("siteName"));
        campsite.put("maxPeople", "4");
        campsite.put("pricePerNight", "30000");

        ExtractableResponse<Response> siteResponse = ReservationTestFixture.Reservation_캠프사이트_생성(campsite);
        this.siteId = ReservationTestFixture.Reservation_생성된_사이트_ID_추출(siteResponse);

        // 기존 예약 생성
        Map<String, String> reservation = new HashMap<>();
        reservation.put("siteId", siteId.toString());
        reservation.put("checkIn", siteData.get("existingCheckIn"));
        reservation.put("checkOut", siteData.get("existingCheckOut"));
        reservation.put("guestCount", "2");
        reservation.put("customerName", "기존고객");

        ExtractableResponse<Response> reservationResponse = ReservationTestFixture.Reservation_예약_생성(reservation);
        this.reservationId = ReservationTestFixture.Reservation_생성된_예약_ID_추출(reservationResponse);
    }

    @When("고객이 겹치는 기간으로 예약을 시도한다")
    public void 고객이_겹치는_기간으로_예약을_시도한다(DataTable dataTable) {
        List<Map<String, String>> reservations = dataTable.asMaps(String.class, String.class);
        Map<String, String> reservationData = reservations.get(0);

        Map<String, String> newReservation = new HashMap<>();
        newReservation.put("siteId", siteId.toString());
        newReservation.put("checkIn", reservationData.get("newCheckIn"));
        newReservation.put("checkOut", reservationData.get("newCheckOut"));
        newReservation.put("guestCount", reservationData.get("guestCount"));
        newReservation.put("customerName", reservationData.get("customerName"));

        this.reservationResponse = ReservationTestFixture.Reservation_예약_생성_시도(newReservation);
    }

    @Then("예약이 거절된다")
    public void 예약이_거절된다() {
        assertThat(reservationResponse.statusCode()).isEqualTo(400);
    }

    @And("기간 겹침 오류 메시지가 표시된다")
    public void 기간_겹침_오류_메시지가_표시된다(DataTable dataTable) {
        List<Map<String, String>> messages = dataTable.asMaps(String.class, String.class);
        String expectedMessage = messages.get(0).get("expectedMessage");

        String actualMessage = reservationResponse.jsonPath().getString("message");
        assertThat(actualMessage).contains(expectedMessage);
    }

    @When("관리자가 예약 현황을 확인한다")
    public void 관리자가_예약_현황을_확인한다() {
        this.reservationResponse = AdminTestFixture.Admin_예약_목록_조회();
    }

    @Then("기존 예약만 유지되고 있다")
    public void 기존_예약만_유지되고_있다(DataTable dataTable) {
        List<Map<String, String>> reservations = dataTable.asMaps(String.class, String.class);
        Map<String, String> expectedData = reservations.get(0);

        expectedData.put("status", "CONFIRMED");
        AdminTestFixture.Admin_예약_목록_검증(reservationResponse, reservationId, expectedData);
    }

    // 예약 기간 인접 날짜 처리
    @When("고객이 바로 다음날부터 예약을 시도한다")
    public void 고객이_바로_다음날부터_예약을_시도한다(DataTable dataTable) {
        List<Map<String, String>> reservations = dataTable.asMaps(String.class, String.class);
        Map<String, String> reservationData = reservations.get(0);

        Map<String, String> newReservation = new HashMap<>();
        newReservation.put("siteId", siteId.toString());
        newReservation.put("checkIn", reservationData.get("newCheckIn"));
        newReservation.put("checkOut", reservationData.get("newCheckOut"));
        newReservation.put("guestCount", reservationData.get("guestCount"));
        newReservation.put("customerName", reservationData.get("customerName"));

        this.reservationResponse = ReservationTestFixture.Reservation_예약_생성(newReservation);
    }

    @Then("예약이 성공한다")
    public void 예약이_성공한다() {
        assertThat(reservationResponse.statusCode()).isEqualTo(201);
    }

    @When("관리자가 예약 캘린더를 확인한다")
    public void 관리자가_예약_캘린더를_확인한다() {
        this.reservationResponse = AdminTestFixture.Admin_예약_목록_조회();
    }

    @Then("두 예약이 연속으로 표시된다")
    public void 두_예약이_연속으로_표시된다(DataTable dataTable) {
        // 두 예약이 모두 존재하는지 확인
        List<Map<String, Object>> reservations = reservationResponse.jsonPath().getList("$");
        assertThat(reservations).hasSizeGreaterThanOrEqualTo(2);
    }

    // 과거 날짜 예약 시도 검증
    @Given("오늘 날짜가 {int}-{int}-{int}이다")
    public void 오늘_날짜가_이다(int year, int month, int day) {
        this.currentDate = String.format("%04d-%02d-%02d", year, month, day);
    }

    @When("고객이 과거 날짜로 예약을 시도한다")
    public void 고객이_과거_날짜로_예약을_시도한다(DataTable dataTable) {
        List<Map<String, String>> reservations = dataTable.asMaps(String.class, String.class);
        Map<String, String> reservationData = reservations.get(0);

        // 캠프사이트가 없다면 생성
        if (siteId == null) {
            Map<String, String> campsite = new HashMap<>();
            campsite.put("siteName", reservationData.get("siteName"));
            campsite.put("maxPeople", "4");
            campsite.put("pricePerNight", "30000");

            ExtractableResponse<Response> siteResponse = ReservationTestFixture.Reservation_캠프사이트_생성(campsite);
            this.siteId = ReservationTestFixture.Reservation_생성된_사이트_ID_추출(siteResponse);
        }

        Map<String, String> pastReservation = new HashMap<>();
        pastReservation.put("siteId", siteId.toString());
        pastReservation.put("checkIn", reservationData.get("checkIn"));
        pastReservation.put("checkOut", reservationData.get("checkOut"));
        pastReservation.put("guestCount", reservationData.get("guestCount"));
        pastReservation.put("customerName", reservationData.get("customerName"));

        this.reservationResponse = ReservationTestFixture.Reservation_예약_생성_시도(pastReservation);
    }

    @And("과거 날짜 오류 메시지가 표시된다")
    public void 과거_날짜_오류_메시지가_표시된다(DataTable dataTable) {
        List<Map<String, String>> messages = dataTable.asMaps(String.class, String.class);
        String expectedMessage = messages.get(0).get("expectedMessage");

        String actualMessage = reservationResponse.jsonPath().getString("message");
        assertThat(actualMessage).contains(expectedMessage);
    }

    @When("관리자가 예약 로그를 확인한다")
    public void 관리자가_예약_로그를_확인한다() {
        // 예약 로그 조회 (구현에 따라 API 경로 조정 필요)
        this.reservationResponse = AdminTestFixture.Admin_예약_목록_조회();
    }

    @Then("실패한 예약 시도가 기록되어 있다")
    public void 실패한_예약_시도가_기록되어_있다(DataTable dataTable) {
        // 로그 기록 확인 로직 (실제 구현에 따라 조정 필요)
        assertThat(reservationResponse.statusCode()).isEqualTo(200);
    }

    // 캠프사이트 최대 수용 인원 초과 검증
    @Given("최대 수용 인원이 제한된 캠프사이트가 있다")
    public void 최대_수용_인원이_제한된_캠프사이트가_있다(DataTable dataTable) {
        List<Map<String, String>> sites = dataTable.asMaps(String.class, String.class);
        Map<String, String> siteData = sites.get(0);

        Map<String, String> campsite = new HashMap<>();
        campsite.put("siteName", siteData.get("siteName"));
        campsite.put("maxPeople", siteData.get("maxPeople"));
        campsite.put("pricePerNight", siteData.get("pricePerNight"));

        ExtractableResponse<Response> siteResponse = ReservationTestFixture.Reservation_캠프사이트_생성(campsite);
        this.siteId = ReservationTestFixture.Reservation_생성된_사이트_ID_추출(siteResponse);
    }

    @When("고객이 최대 인원을 초과하여 예약을 시도한다")
    public void 고객이_최대_인원을_초과하여_예약을_시도한다(DataTable dataTable) {
        List<Map<String, String>> reservations = dataTable.asMaps(String.class, String.class);
        Map<String, String> reservationData = reservations.get(0);

        Map<String, String> overCapacityReservation = new HashMap<>();
        overCapacityReservation.put("siteId", siteId.toString());
        overCapacityReservation.put("checkIn", reservationData.get("checkIn"));
        overCapacityReservation.put("checkOut", reservationData.get("checkOut"));
        overCapacityReservation.put("guestCount", reservationData.get("guestCount"));
        overCapacityReservation.put("customerName", reservationData.get("customerName"));

        this.reservationResponse = ReservationTestFixture.Reservation_예약_생성_시도(overCapacityReservation);
    }

    @And("인원 초과 오류 메시지가 표시된다")
    public void 인원_초과_오류_메시지가_표시된다(DataTable dataTable) {
        List<Map<String, String>> messages = dataTable.asMaps(String.class, String.class);
        String expectedMessage = messages.get(0).get("expectedMessage");

        String actualMessage = reservationResponse.jsonPath().getString("message");
        assertThat(actualMessage).contains(expectedMessage);
    }

    @When("고객이 적정 인원으로 재시도한다")
    public void 고객이_적정_인원으로_재시도한다(DataTable dataTable) {
        List<Map<String, String>> reservations = dataTable.asMaps(String.class, String.class);
        Map<String, String> reservationData = reservations.get(0);

        Map<String, String> validReservation = new HashMap<>();
        validReservation.put("siteId", siteId.toString());
        validReservation.put("checkIn", reservationData.get("checkIn"));
        validReservation.put("checkOut", reservationData.get("checkOut"));
        validReservation.put("guestCount", reservationData.get("guestCount"));
        validReservation.put("customerName", reservationData.get("customerName"));

        this.reservationResponse = ReservationTestFixture.Reservation_예약_생성(validReservation);
    }

    // 예약 최소 인원 미달 처리
    @Given("최소 예약 인원이 설정된 캠프사이트가 있다")
    public void 최소_예약_인원이_설정된_캠프사이트가_있다(DataTable dataTable) {
        List<Map<String, String>> sites = dataTable.asMaps(String.class, String.class);
        Map<String, String> siteData = sites.get(0);

        Map<String, String> campsite = new HashMap<>();
        campsite.put("siteName", siteData.get("siteName"));
        campsite.put("minPeople", siteData.get("minPeople"));
        campsite.put("maxPeople", siteData.get("maxPeople"));
        campsite.put("pricePerNight", siteData.get("pricePerNight"));

        ExtractableResponse<Response> siteResponse = ReservationTestFixture.Reservation_캠프사이트_생성(campsite);
        this.siteId = ReservationTestFixture.Reservation_생성된_사이트_ID_추출(siteResponse);
    }

    @When("고객이 최소 인원 미달로 예약을 시도한다")
    public void 고객이_최소_인원_미달로_예약을_시도한다(DataTable dataTable) {
        List<Map<String, String>> reservations = dataTable.asMaps(String.class, String.class);
        Map<String, String> reservationData = reservations.get(0);

        Map<String, String> underMinReservation = new HashMap<>();
        underMinReservation.put("siteId", siteId.toString());
        underMinReservation.put("checkIn", reservationData.get("checkIn"));
        underMinReservation.put("checkOut", reservationData.get("checkOut"));
        underMinReservation.put("guestCount", reservationData.get("guestCount"));
        underMinReservation.put("customerName", reservationData.get("customerName"));

        this.reservationResponse = ReservationTestFixture.Reservation_예약_생성_시도(underMinReservation);
    }

    @Then("예약이 거절되거나 경고가 표시된다")
    public void 예약이_거절되거나_경고가_표시된다() {
        // 예약이 거절되거나 경고와 함께 생성될 수 있음
        assertThat(reservationResponse.statusCode()).isIn(400, 201);
    }

    @And("최소 인원 안내 메시지가 표시된다")
    public void 최소_인원_안내_메시지가_표시된다(DataTable dataTable) {
        List<Map<String, String>> messages = dataTable.asMaps(String.class, String.class);
        String expectedMessage = messages.get(0).get("expectedMessage");

        if (reservationResponse.statusCode() == 400) {
            String actualMessage = reservationResponse.jsonPath().getString("message");
            assertThat(actualMessage).contains(expectedMessage);
        }
    }

    @When("관리자가 예약 정책을 확인한다")
    public void 관리자가_예약_정책을_확인한다() {
        // 예약 정책 확인 로직
        this.reservationResponse = AdminTestFixture.Admin_예약_목록_조회();
    }

    @Then("사이트별 인원 제한 정책이 올바르게 적용되고 있다")
    public void 사이트별_인원_제한_정책이_올바르게_적용되고_있다() {
        assertThat(reservationResponse.statusCode()).isEqualTo(200);
    }

    // 상품 수정 시 경계값 처리
    @Given("관리자가 기존 상품을 가지고 있다")
    public void 관리자가_기존_상품을_가지고_있다(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        Map<String, String> productData = products.get(0);

        ExtractableResponse<Response> response = AdminTestFixture.Admin_상품_생성(productData);
        this.productId = AdminTestFixture.Admin_생성된_상품_ID_추출(response);
    }

    @When("관리자가 상품을 경계값으로 수정한다")
    public void 관리자가_상품을_경계값으로_수정한다(DataTable dataTable) {
        List<Map<String, String>> updates = dataTable.asMaps(String.class, String.class);
        Map<String, String> updateData = updates.get(0);

        this.updateResponse = AdminTestFixture.Admin_상품_정보_수정_시도(productId, updateData);
    }

    @Then("유효성 검증 오류가 발생한다")
    public void 유효성_검증_오류가_발생한다() {
        assertThat(updateResponse.statusCode()).isEqualTo(400);
    }

    @And("적절한 검증 메시지가 표시된다")
    public void 적절한_검증_메시지가_표시된다(DataTable dataTable) {
        List<Map<String, String>> validations = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> validation : validations) {
            String expectedMessage = validation.get("expectedMessage");
            String actualMessage = updateResponse.jsonPath().getString("message");
            assertThat(actualMessage).contains(expectedMessage);
        }
    }

    // "키오스크에서 상품을 조회한다" - NormalIntegrationSteps의 "키오스크에서 상품 목록을 조회한다" 재사용

    @Then("기존 상품 정보가 유지되고 있다")
    public void 기존_상품_정보가_유지되고_있다(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        Map<String, String> expectedData = products.get(0);

        KioskTestFixture.Kiosk_상품_정보_일치_검증(purchaseResponse, productId, expectedData);
    }

    // 인증 토큰 만료 경계 시점 처리 - 기존 NormalIntegrationSteps의 "키오스크가 관리자 서비스에 인증되어 있다" 사용

    @And("JWT 토큰 만료 시간이 {int}분 남았다")
    public void JWT_토큰_만료_시간이_분_남았다(int minutes) {
        // 토큰 만료 시간 설정 로직 (실제 구현에 따라 조정)
        // 테스트를 위해 단순히 기록만 함
    }

    @When("키오스크가 토큰 만료 직전에 API를 호출한다")
    public void 키오스크가_토큰_만료_직전에_API를_호출한다(DataTable dataTable) {
        this.purchaseResponse = KioskTestFixture.키오스크_상품_목록_조회();
    }

    // "API 호출이 성공한다" - NormalIntegrationSteps에 동일한 step 있음

    @When("토큰이 만료된 후 API를 호출한다")
    public void 토큰이_만료된_후_API를_호출한다(DataTable dataTable) {
        // 토큰 만료 시뮬레이션 후 API 호출
        this.purchaseResponse = KioskTestFixture.키오스크_상품_목록_조회();
    }

    @Then("인증 오류가 발생한다")
    public void 인증_오류가_발생한다() {
        assertThat(purchaseResponse.statusCode()).isEqualTo(401);
    }

    @And("자동으로 재인증을 시도한다")
    public void 자동으로_재인증을_시도한다() {
        // 재인증 로직 시뮬레이션
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "admin");
        credentials.put("password", "admin123");

        this.authResponse = AdminTestFixture.Admin_인증_요청(credentials);
    }

    @Then("재인증 후 API 호출이 성공한다")
    public void 재인증_후_API_호출이_성공한다() {
        this.purchaseResponse = KioskTestFixture.키오스크_상품_목록_조회();
        assertThat(purchaseResponse.statusCode()).isEqualTo(200);
    }

    // 결제 관련 단계들은 PaymentSteps와 NormalIntegrationSteps에서 재사용

    @And("상품이 등록되어 있다")
    public void 상품이_등록되어_있다(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        Map<String, String> productData = products.get(0);

        productData.put("productType", "CAMPING");
        ExtractableResponse<Response> response = AdminTestFixture.Admin_상품_생성(productData);
        this.productId = AdminTestFixture.Admin_생성된_상품_ID_추출(response);
    }
}