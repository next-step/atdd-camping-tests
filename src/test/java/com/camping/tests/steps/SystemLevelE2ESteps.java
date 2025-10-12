package com.camping.tests.steps;

import com.camping.tests.context.CommonContextHolder;
import com.camping.tests.helpers.AdminApiHelper;
import com.camping.tests.helpers.KioskApiHelper;
import com.camping.tests.helpers.ReservationApiHelper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemLevelE2ESteps {

    private String reservationId;
    private String confirmationCode;

    private CommonContextHolder context() {
        return CommonContextHolder.getInstance();
    }

    @Given("예약 서비스에서 이용 가능한 사이트를 조회한다")
    public void getAvailableSites() {
        Response response = ReservationApiHelper.getAvailableSites("2025-12-01");
        context().setResponse(response);
        response.then().statusCode(200);
    }

    @Given("사이트 {int}번에 대해 {string}부터 {string}까지 예약을 생성한다")
    public void givenCreateReservationForSite(Integer siteNum, String startDate, String endDate) {
        Response response = ReservationApiHelper.createReservation(siteNum, startDate, endDate);
        context().setResponse(response);
        response.then().statusCode(201);

        reservationId = response.jsonPath().getString("id");
        confirmationCode = response.jsonPath().getString("confirmationCode");
    }

    @Given("예약 서비스에서 {string}부터 {string}까지 예약을 생성한다")
    public void createReservation(String startDate, String endDate) {
        givenCreateReservationForSite(11, startDate, endDate);
    }

    @Then("예약이 성공적으로 생성되고 확인 코드를 받는다")
    public void verifyReservationCreated() {
        Response response = context().getResponse();
        response.then().statusCode(201);

        assertThat(reservationId).isNotNull();
        assertThat(confirmationCode).isNotNull();
    }

    @When("예약 확인 코드로 예약을 조회한다")
    public void getReservationByConfirmationCode() {
        Response response = ReservationApiHelper.getReservationByConfirmationCode(confirmationCode);
        context().setResponse(response);
    }

    @When("예약 서비스에서 해당 예약을 조회한다")
    public void getReservationById() {
        Response response = ReservationApiHelper.getReservationById(reservationId);
        context().setResponse(response);
    }

    @Then("예약 상태가 {string} 이다")
    public void verifyReservationStatus(String expectedStatus) {
        Response response = context().getResponse();
        response.then().statusCode(200);

        Object statusObj = response.jsonPath().get("status");
        String status;
        if (statusObj instanceof List) {
            status = response.jsonPath().getString("[0].status");
        } else {
            status = response.jsonPath().getString("status");
        }
        assertThat(status).isEqualTo(expectedStatus);
    }

    @When("확인 코드를 사용하여 예약을 취소한다")
    public void cancelReservation() {
        Response response = ReservationApiHelper.cancelReservation(reservationId, confirmationCode);
        context().setResponse(response);
    }

    @Then("예약 취소가 성공한다")
    public void verifyCancellationSuccess() {
        Response response = context().getResponse();
        response.then().statusCode(200);
    }

    @When("관리자가 모든 예약을 조회한다")
    public void adminGetAllReservations() {
        Response response = AdminApiHelper.getAllReservations();
        context().setResponse(response);
    }

    @Then("방금 생성한 예약이 목록에 포함되어 있다")
    public void verifyReservationInList() {
        Response response = context().getResponse();
        response.then().statusCode(200);

        List<String> ids = response.jsonPath().getList("id", String.class);
        assertThat(ids).contains(reservationId);
    }

    @When("관리자가 해당 예약의 상태를 {string}로 변경한다")
    public void adminUpdateReservationStatus(String newStatus) {
        Response response = AdminApiHelper.updateReservationStatus(reservationId, newStatus);
        context().setResponse(response);
    }

    @Then("예약 상태 변경이 성공한다")
    public void verifyStatusUpdateSuccess() {
        Response response = context().getResponse();
        response.then().statusCode(200);
    }

    @When("관리자가 판매 기록을 조회한다")
    public void adminGetSalesRecords() {
        Response response = AdminApiHelper.getSalesRecords();
        context().setResponse(response);
    }

    @Then("최근 판매 기록이 조회된다")
    public void verifySalesRecords() {
        Response response = context().getResponse();
        response.then().statusCode(200);

        List<?> sales = response.jsonPath().getList("$");
        assertThat(sales).isNotEmpty();
    }

    @When("키오스크에서 상품 목록을 조회한다")
    public void getProductList() {
        Response response = KioskApiHelper.getProductList();
        context().setResponse(response);
    }

}
