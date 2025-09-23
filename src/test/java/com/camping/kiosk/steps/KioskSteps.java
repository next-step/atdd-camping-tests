package com.camping.kiosk.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.camping.common.support.CommonContext;
import com.camping.common.support.KioskApiHelper;
import io.cucumber.core.options.CurlOption.HttpMethod;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;

import java.util.List;

public class KioskSteps {

    public KioskSteps() {
        RestAssured.baseURI = CommonContext.KIOSK_BASE_URL;
    }

    @When("키오스크 컨테이너에 요청을 보낸다")
    public void 키오스크컨테이너에요청을보낸다() {
        CommonContext.lastResponse = KioskApiHelper.request(HttpMethod.GET, "/", null)
                .then().log().all()
                .extract().response();
    }

    @When("키오스크에서 전체 상품 조회를 요청한다")
    public void 키오스크에서전체상품조회를요청한다() {
        CommonContext.lastResponse = KioskApiHelper.request(HttpMethod.GET, "api/products", null);
    }

    @And("{int}개 이상의 상품 정보가 확인된다")
    public void 개이상의상품정보가확인된다(int count) {
        JsonPath jsonPath = CommonContext.lastResponse.then().log().all()
                .extract().response().jsonPath();
        List<Object> products = jsonPath.getList("$");
        assertThat(products.size()).isGreaterThanOrEqualTo(count);
    }
}


