package com.camping.tests.helper;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/**
 * 키오스크 API 요청 전송 유틸리티
 */
public class KioskRequestSender {
    
    public static Response get(String endpoint) {
        System.out.println("📤 키오스크 " + endpoint + "에 요청을 보냅니다.");
        
        Response response = given()
            .when()
            .get(endpoint)
            .then()
            .extract()
            .response();
            
        System.out.println("📥 응답 받음: " + response.getStatusCode());
        return response;
    }
    
    public static Response getWithTiming(String endpoint) {
        System.out.println("📤 키오스크 " + endpoint + "에 요청을 보냅니다.");
        KioskContext.requestStartTime = System.currentTimeMillis();
        
        Response response = given()
            .when()
            .get(endpoint)
            .then()
            .extract()
            .response();
            
        System.out.println("📥 응답 받음: " + response.getStatusCode());
        return response;
    }
}
