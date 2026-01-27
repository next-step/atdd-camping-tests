package com.camping.tests.steps;

import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만약;
import io.cucumber.java.ko.먼저;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KioskSmokeSteps {

    private static final String KIOSK_BASE_URL = System.getenv("KIOSK_BASE_URL") != null ? System.getenv("KIOSK_BASE_URL") : "http://localhost:18081";

    private Response response;


    @먼저("Kiosk 서비스가 준비될 때까지 대기한다")
    public void kiosk_서비스가_준비될_때까지_대기한다() {
        int maxRetries = 30;
        int retryInterval = 2000;

        for (int i = 0; i < maxRetries; i++) {
            try {
                Response healthResponse = RestAssured
                        .given()
                        .baseUri(KIOSK_BASE_URL)
                        .when()
                        .get("/");

                if (healthResponse.getStatusCode() == 200) {
                    System.out.println("Kiosk 서비스 준비 완료 (시도: " + (i + 1) + ")");
                    return;
                }

            } catch (Exception e) {
                System.out.println("Kiosk 서비스 대기 중... (" + (i + 1) + "/" + maxRetries + ")");
            }

            try {
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        throw new RuntimeException("Kiosk 서비스가 준비되지 않았습니다.");
    }

    @만약("Kiosk의 {string} 엔드포인트에 GET 요청을 보낸다")
    public void kiosk의_엔드포인트에_get_요청을_보낸다(String endpoint) {
        response = RestAssured.given()
                .baseUri(KIOSK_BASE_URL)
                .when()
                .get(endpoint);
    }

    @그러면("응답 상태코드는 {int}이어야 한다")
    public void 응답_상태코드는_이어야_한다(int expectedStatusCode) {
        assertEquals(expectedStatusCode, response.getStatusCode());
    }
}
