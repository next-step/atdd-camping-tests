package com.camping.reservation.steps;

import com.camping.common.support.CommonContext;
import com.camping.common.support.ReservationApiHelper;
import io.cucumber.core.options.CurlOption.HttpMethod;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;

public class ReservationSteps {

    public ReservationSteps() {
        RestAssured.baseURI = CommonContext.RESERVATION_BASE_URL;
    }

    @When("reservation 컨테이너에 요청을 보낸다")
    public void reservation컨테이너에요청을보낸다() {
        CommonContext.lastResponse = ReservationApiHelper.request(HttpMethod.GET, "/", null)
                .then().log().all()
                .extract().response();
    }
}
