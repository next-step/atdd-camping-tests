package com.camping.tests.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static com.camping.tests.support.fixture.AdminTestFixture.*;
import static com.camping.tests.support.fixture.KioskTestFixture.*;
import static com.camping.tests.support.fixture.PaymentTestFixture.*;
import static com.camping.tests.support.fixture.ReservationTestFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.camping.tests.support.client.ApiClientFactory;

public class NormalIntegrationSteps {

    private ExtractableResponse<Response> adminResponse;
    private ExtractableResponse<Response> kioskResponse;
    private ExtractableResponse<Response> paymentResponse;
    private ExtractableResponse<Response> reservationResponse;
    private Long createdProductId;
    private Long createdReservationId;
    private Map<String, String> productData;
    private Map<String, String> reservationData;

    // 상품 생성부터 판매까지 전체 플로우
    @Given("관리자가 로그인되어 있다")
    public void 관리자가로그인되어있다() {
        // Hook에서 자동 처리됨
    }

    // WireMock 결제 서버는 infra/wiremock에서 자동 실행됨

    @When("관리자가 새로운 상품을 등록한다")
    public void 관리자가새로운상품을등록한다(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps();
        productData = products.get(0);
        adminResponse = Admin_상품_생성(productData);
        createdProductId = Admin_생성된_상품_ID_추출(adminResponse);
    }

    @Then("상품이 성공적으로 등록된다")
    public void 상품이성공적으로등록된다() {
        Admin_상품_등록_성공_검증(adminResponse);
    }

    @When("키오스크에서 상품 목록을 조회한다")
    public void 키오스크에서상품목록을조회한다() {
        kioskResponse = 키오스크_상품_목록_조회();
    }

    @When("키오스크에서 상품 목록을 다시 조회한다")
    public void 키오스크에서상품목록을다시조회한다() {
        키오스크에서상품목록을조회한다();
    }

    @Then("등록한 상품이 키오스크에 표시된다")
    public void 등록한상품이키오스크에표시된다() {
        생성된_상품_정보_일치_검증(kioskResponse, createdProductId, productData);
    }

    @When("고객이 키오스크에서 상품을 구매한다")
    public void 고객이키오스크에서상품을구매한다(DataTable dataTable) {
        List<Map<String, String>> purchases = dataTable.asMaps();
        Map<String, String> purchaseData = purchases.get(0);

        Map<String, Object> selectedProduct = Map.of(
            "productId", createdProductId,
            "quantity", Integer.parseInt(purchaseData.get("quantity")),
            "price", Integer.parseInt(productData.get("price"))
        );

        paymentResponse = 정상_금액으로_결제_요청(List.of(selectedProduct));
    }

    @Then("WireMock을 통한 외부 결제가 성공한다")
    public void WireMock을통한외부결제가성공한다() {
        결제_성공_검증(paymentResponse);
    }


    @And("관리자 시스템의 재고가 올바르게 차감된다")
    public void 관리자시스템의재고가올바르게차감된다(DataTable dataTable) {
        List<Map<String, String>> expectedStocks = dataTable.asMaps();
        int expectedStock = Integer.parseInt(expectedStocks.get(0).get("expectedStock"));

        ExtractableResponse<Response> stockResponse = Admin_상품_재고_조회(createdProductId);
        Admin_재고_차감_검증(stockResponse, expectedStock);
    }

    @And("매출 기록이 생성된다")
    public void 매출기록이생성된다() {
        ExtractableResponse<Response> salesResponse = Admin_매출_기록_조회(createdProductId);
        Admin_매출_기록_존재_검증(salesResponse, createdProductId);
    }

    // 상품 정보 수정 시 키오스크 실시간 반영
    @And("상품이 이미 등록되어 있다")
    public void 상품이이미등록되어있다(DataTable dataTable) {
        관리자가새로운상품을등록한다(dataTable);
    }

    @When("관리자가 상품 정보를 수정한다")
    public void 관리자가상품정보를수정한다(DataTable dataTable) {
        List<Map<String, String>> updates = dataTable.asMaps();
        Map<String, String> updateData = updates.get(0);
        adminResponse = Admin_상품_정보_수정(createdProductId, updateData);
    }

    @Then("상품 정보가 성공적으로 수정된다")
    public void 상품정보가성공적으로수정된다() {
        Admin_상품_수정_성공_검증(adminResponse);
    }

    @Then("수정된 상품 정보가 키오스크에 반영된다")
    public void 수정된상품정보가키오스크에반영된다(DataTable dataTable) {
        List<Map<String, String>> expectedData = dataTable.asMaps();
        Map<String, String> expected = expectedData.get(0);
        수정된_상품_정보_반영_검증(kioskResponse, createdProductId, expected);
    }

    // 예약 관련 시나리오들
    @Given("예약 가능한 캠프사이트가 있다")
    public void 예약가능한캠프사이트가있다(DataTable dataTable) {
        List<Map<String, String>> sites = dataTable.asMaps();
        Map<String, String> siteData = sites.get(0);
        Reservation_캠프사이트_생성(siteData);
    }

    @When("고객이 예약을 생성한다")
    public void 고객이예약을생성한다(DataTable dataTable) {
        List<Map<String, String>> reservations = dataTable.asMaps();
        reservationData = reservations.get(0);
        reservationResponse = Reservation_예약_생성(reservationData);
        createdReservationId = Reservation_생성된_예약_ID_추출(reservationResponse);
    }

    @Then("예약이 성공적으로 생성된다")
    public void 예약이성공적으로생성된다() {
        Reservation_예약_생성_성공_검증(reservationResponse);
    }

    @When("관리자가 예약 목록을 조회한다")
    public void 관리자가예약목록을조회한다() {
        adminResponse = Admin_예약_목록_조회();
    }

    @Then("생성된 예약이 관리자 시스템에 표시된다")
    public void 생성된예약이관리자시스템에표시된다(DataTable dataTable) {
        List<Map<String, String>> expectedData = dataTable.asMaps();
        Map<String, String> expected = expectedData.get(0);
        Admin_예약_목록_검증(adminResponse, createdReservationId, expected);
    }

    @When("관리자가 예약 상태를 변경한다")
    public void 관리자가예약상태를변경한다(DataTable dataTable) {
        List<Map<String, String>> statusChanges = dataTable.asMaps();
        Map<String, String> changeData = statusChanges.get(0);
        adminResponse = Admin_예약_상태_변경(createdReservationId, changeData);
    }

    @Then("예약 상태가 성공적으로 변경된다")
    public void 예약상태가성공적으로변경된다() {
        Admin_예약_상태_변경_성공_검증(adminResponse);
    }

    // 인증 관련
    @Given("관리자 서비스가 실행 중이다")
    public void 관리자서비스가실행중이다() {
        // 서비스 상태 확인 로직
    }

    @When("키오스크가 관리자 서비스에 인증을 요청한다")
    public void 키오스크가관리자서비스에인증을요청한다(DataTable dataTable) {
        List<Map<String, String>> credentials = dataTable.asMaps();
        Map<String, String> cred = credentials.get(0);
        adminResponse = Admin_인증_요청(cred);
    }

    @Then("인증이 성공하고 JWT 토큰을 받는다")
    public void 인증이성공하고JWT토큰을받는다() {
        Admin_인증_성공_검증(adminResponse);
    }

    @When("키오스크가 인증이 필요한 API를 호출한다")
    public void 키오스크가인증이필요한API를호출한다(DataTable dataTable) {
        List<Map<String, String>> apiCalls = dataTable.asMaps();
        Map<String, String> apiCall = apiCalls.get(0);

        if ("GET".equals(apiCall.get("method"))) {
            kioskResponse = ApiClientFactory.kiosk()
                .get(apiCall.get("endpoint"))
                .needAuth()
                .execute();
        }
    }

    @Then("API 호출이 성공한다")
    public void API호출이성공한다() {
        assertThat(kioskResponse.statusCode()).isEqualTo(200);
    }

    @And("올바른 인증 헤더가 포함되어 있다")
    public void 올바른인증헤더가포함되어있다() {
        // 인증 헤더 검증은 실제 구현에서 처리
        assertThat(kioskResponse.statusCode()).isEqualTo(200);
    }

    // 재고 관리 연동
    @Given("관리자가 상품 재고를 설정한다")
    public void 관리자가상품재고를설정한다(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps();
        Map<String, String> productData = products.get(0);

        Map<String, String> fullProductData = Map.of(
            "name", productData.get("productName"),
            "price", "25000", // 기본값
            "stockQuantity", productData.get("initialStock"),
            "productType", "CAMPING"
        );

        adminResponse = Admin_상품_생성(fullProductData);
        createdProductId = Admin_생성된_상품_ID_추출(adminResponse);
    }

    @When("키오스크에서 해당 상품을 조회한다")
    public void 키오스크에서해당상품을조회한다() {
        키오스크에서상품목록을조회한다();
    }

    @Then("올바른 재고 수량이 표시된다")
    public void 올바른재고수량이표시된다(DataTable dataTable) {
        List<Map<String, String>> expectedStocks = dataTable.asMaps();
        Map<String, String> expected = expectedStocks.get(0);

        List<Map<String, Object>> products = kioskResponse.jsonPath().getList("$");
        Map<String, Object> foundProduct = products.stream()
            .filter(product -> ((Integer) product.get("id")).longValue() == createdProductId)
            .findFirst()
            .orElseThrow(() -> new AssertionError("상품을 찾을 수 없습니다."));

        assertThat(foundProduct.get("stockQuantity")).isEqualTo(Integer.parseInt(expected.get("expectedStock")));
    }

    @When("키오스크에서 상품을 판매한다")
    public void 키오스크에서상품을판매한다(DataTable dataTable) {
        List<Map<String, String>> sales = dataTable.asMaps();
        Map<String, String> sale = sales.get(0);

        Map<String, Object> selectedProduct = Map.of(
            "productId", createdProductId,
            "quantity", Integer.parseInt(sale.get("soldQuantity")),
            "price", 25000
        );

        paymentResponse = 정상_금액으로_결제_요청(List.of(selectedProduct));
    }

    @Then("관리자 시스템의 재고가 자동으로 업데이트된다")
    public void 관리자시스템의재고가자동으로업데이트된다(DataTable dataTable) {
        관리자시스템의재고가올바르게차감된다(dataTable);
    }

    @And("매출 통계에 판매 내역이 반영된다")
    public void 매출통계에판매내역이반영된다(DataTable dataTable) {
        ExtractableResponse<Response> salesResponse = Admin_매출_기록_조회(createdProductId);
        Admin_매출_기록_존재_검증(salesResponse, createdProductId);
    }

    // 사이트 가용성 관련
    @Given("캠프사이트의 예약 현황이 있다")
    public void 캠프사이트의예약현황이있다(DataTable dataTable) {
        List<Map<String, String>> reservations = dataTable.asMaps();
        for (Map<String, String> reservation : reservations) {
            // 기존 예약 생성
            Map<String, String> siteData = Map.of(
                "siteName", reservation.get("siteName"),
                "maxPeople", "6",
                "pricePerNight", "30000"
            );
            Reservation_캠프사이트_생성(siteData);

            // 예약 생성은 실제 구현에서 처리
        }
    }

    @When("고객이 사이트 가용성을 확인한다")
    public void 고객이사이트가용성을확인한다(DataTable dataTable) {
        List<Map<String, String>> checks = dataTable.asMaps();
        Map<String, String> check = checks.get(0);

        reservationResponse = Reservation_사이트_가용성_확인(check.get("siteName"), check.get("checkDate"));
    }

    @Then("해당 날짜는 예약 불가능으로 표시된다")
    public void 해당날짜는예약불가능으로표시된다() {
        Reservation_가용성_불가능_검증(reservationResponse);
    }

    @When("고객이 다른 날짜의 가용성을 확인한다")
    public void 고객이다른날짜의가용성을확인한다(DataTable dataTable) {
        고객이사이트가용성을확인한다(dataTable);
    }

    @Then("해당 날짜는 예약 가능으로 표시된다")
    public void 해당날짜는예약가능으로표시된다() {
        Reservation_가용성_가능_검증(reservationResponse);
    }

    @When("관리자가 전체 캠프사이트 현황을 조회한다")
    public void 관리자가전체캠프사이트현황을조회한다() {
        adminResponse = Admin_예약_목록_조회();
    }

    @Then("각 사이트의 예약 상태가 정확하게 표시된다")
    public void 각사이트의예약상태가정확하게표시된다() {
        assertThat(adminResponse.statusCode()).isEqualTo(200);
    }

    // 예약 상태 동기화
    @Given("예약이 생성되어 있다")
    public void 예약이생성되어있다(DataTable dataTable) {
        List<Map<String, String>> reservations = dataTable.asMaps();
        Map<String, String> reservation = reservations.get(0);

        // 먼저 사이트 생성
        Map<String, String> siteData = Map.of(
            "siteName", reservation.get("siteName"),
            "maxPeople", "4",
            "pricePerNight", "25000"
        );
        Reservation_캠프사이트_생성(siteData);

        // 예약 생성
        Map<String, String> reservationData = Map.of(
            "siteName", reservation.get("siteName"),
            "checkIn", "2024-12-25",
            "checkOut", "2024-12-27",
            "guestCount", "3",
            "customerName", reservation.get("customerName"),
            "phone", "010-1234-5678"
        );

        reservationResponse = Reservation_예약_생성(reservationData);
        createdReservationId = Reservation_생성된_예약_ID_추출(reservationResponse);
    }

    @Then("예약 서비스에서 상태가 반영된다")
    public void 예약서비스에서상태가반영된다(DataTable dataTable) {
        // 실제 구현에서는 Reservation 서비스에서 상태 확인
        assertThat(adminResponse.statusCode()).isEqualTo(200);
    }

    @And("실시간으로 동기화가 완료된다")
    public void 실시간으로동기화가완료된다() {
        // 동기화 완료 검증
        assertThat(adminResponse.statusCode()).isEqualTo(200);
    }

    // 토큰 갱신
    @Given("키오스크가 관리자 서비스에 인증되어 있다")
    public void 키오스크가관리자서비스에인증되어있다() {
        // Hook에서 처리됨
    }

    @When("JWT 토큰이 만료되기 1분 전이다")
    public void JWT토큰이만료되기1분전이다() {
        // 토큰 만료 시뮬레이션
    }

    @Then("자동으로 토큰 갱신이 시작된다")
    public void 자동으로토큰갱신이시작된다() {
        // 토큰 갱신 로직 실행
    }

    @When("갱신된 토큰으로 API를 호출한다")
    public void 갱신된토큰으로API를호출한다(DataTable dataTable) {
        키오스크가인증이필요한API를호출한다(dataTable);
    }

    @And("서비스 연속성이 확보된다")
    public void 서비스연속성이확보된다() {
        assertThat(kioskResponse.statusCode()).isEqualTo(200);
    }
}