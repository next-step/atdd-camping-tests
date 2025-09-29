package com.camping.tests.scenario.reservation;

import com.camping.tests.scenario.TestContext;
import com.camping.tests.steps.admin.AdminReservationTestSteps;
import com.camping.tests.steps.admin.AdminAuthTestSteps;
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

        // 예약 ID 저장
        var 예약_정보 = 예약_정보를_가져온다(예약_생성_응답);
        TestContext.Reservation.예약_ID(예약_정보.id());
    }

    @Then("예약 확인 코드가 생성된다")
    public void 예약_확인_코드가_생성된다() {
        var 예약_생성_응답 = TestContext.Reservation.예약_생성_응답();
        예약_확인_코드_생성_확인(예약_생성_응답);
    }

    @When("관리자가 로그인한다")
    public void 관리자가_로그인한다() {
        AdminAuthTestSteps.관리자로_로그인한다();
    }

    @When("관리자가 예약 상태를 {string}로 변경한다")
    public void 관리자가_예약_상태를_변경한다(String status) {
        var 예약_ID = TestContext.Reservation.예약_ID();
        var 상태_변경_응답 = AdminReservationTestSteps.예약_상태를_변경한다(예약_ID, status);
        TestContext.Reservation.예약_상태_변경_응답(상태_변경_응답);
    }

    @Then("예약 상태 변경이 성공한다")
    public void 예약_상태_변경이_성공한다() {
        var 상태_변경_응답 = TestContext.Reservation.예약_상태_변경_응답();
        AdminReservationTestSteps.예약_상태_변경이_성공한다(상태_변경_응답);
    }

    @Then("예약 상태가 {string}로 변경된다")
    public void 예약_상태가_변경된다(String expectedStatus) {
        var 상태_변경_응답 = TestContext.Reservation.예약_상태_변경_응답();
        AdminReservationTestSteps.예약_상태가_변경되었다(상태_변경_응답, expectedStatus);
    }
}
