package com.camping.tests.scenario.reservation;

import com.camping.tests.scenario.TestContext;
import com.camping.tests.steps.reservation.dto.ReservationRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.sql.SQLException;
import java.time.LocalDate;

import static com.camping.tests.steps.reservation.ReservationTestSteps.*;
import static com.camping.tests.steps.reservation.CampsiteTestSteps.*;

public class ReservationScenarioSteps {

    @Given("캠프사이트 {string}이 사용 가능하다")
    public void 캠프사이트가_사용_가능하다(String siteNumber) throws SQLException {
        캠프사이트를_생성한다(siteNumber);
    }

    @When("{string}이 {string}부터 {string}까지 {string} 사이트를 예약한다")
    public void 이_부터_까지_사이트를_예약한다(String customerName, String startDate, String endDate, String siteNumber) {
        var 예약_생성_응답 = 예약을_생성한다(
            ReservationRequest.fixture()
                .customerName(customerName)
                .startDate(LocalDate.parse(startDate))
                .endDate(LocalDate.parse(endDate))
                .siteNumber(siteNumber)
                .create()
        );
        TestContext.Reservation.예약_생성_응답(예약_생성_응답);
    }

    @Then("예약이 성공한다")
    public void 예약이_성공한다() {
        var 예약_생성_응답 = TestContext.Reservation.예약_생성_응답();
        예약_성공_확인(예약_생성_응답);
    }

    @Then("예약 확인 코드가 생성된다")
    public void 예약_확인_코드가_생성된다() {
        var 예약_생성_응답 = TestContext.Reservation.예약_생성_응답();
        예약_확인_코드_생성_확인(예약_생성_응답);
    }
}