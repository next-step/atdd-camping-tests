package com.camping.tests.steps.kiosk;

import static org.assertj.core.api.Assertions.assertThat;

import com.camping.tests.steps.kiosk.dto.KioskConfirmPaymentRequest;
import com.camping.tests.steps.kiosk.dto.KioskCreatePaymentRequest;
import com.camping.tests.steps.kiosk.dto.KioskCreatePaymentResult;
import com.camping.tests.steps.kiosk.dto.KioskProductDetail;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import java.util.List;
import org.apache.http.HttpStatus;

public class KioskProductTestSteps {
    // 상품 목록 조회
    public static KioskProductDetail 상품_목록에서_상품이_조회된다(String targetName) {
        var 상품_목록_조회_응답 = 상품_목록을_조회한다();
        상품_목록_조회가_성공한다(상품_목록_조회_응답);
        return 상품_목록에_상품이_있다(상품_목록_조회_응답, targetName);
    }

    public static void 상품_목록에서_상품이_조회되지_않는다(String targetName) {
        var 상품_목록_조회_응답 = 상품_목록을_조회한다();
        상품_목록_조회가_성공한다(상품_목록_조회_응답);
        상품_목록에_상품이_없다(상품_목록_조회_응답, targetName);
    }

    public static Response 상품_목록을_조회한다() {
        return KioskClient.given()
            .get("/api/products")
            .thenReturn();
    }

    public static void 상품_목록_조회가_성공한다(Response response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    }

    public static KioskProductDetail 상품_목록에_상품이_있다(Response response, String targetName) {
        var productDetails = response.as(new TypeRef<List<KioskProductDetail>>() {
        });
        return productDetails.stream()
            .filter(productDetail -> productDetail.name().equals(targetName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("상품 목록에 " + targetName + " 상품이 존재하지 않습니다."));
    }

    public static void 상품_목록에_상품이_없다(Response response, String targetName) {
        var productDetails = response.as(new TypeRef<List<KioskProductDetail>>() {
        });
        assertThat(productDetails)
            .extracting(KioskProductDetail::name)
            .doesNotContain(targetName);
    }

    // 결제 생성
    public static Response 결제를_생성한다(KioskCreatePaymentRequest request) {
        return KioskClient.given()
            .body(request)
            .post("/api/payments")
            .thenReturn();
    }

    public static void 결제_생성이_성공한다(Response response) {
        response.then().statusCode(HttpStatus.SC_OK);
    }

    public static KioskCreatePaymentResult 결제_생성_결과를_가져온다(Response response) {
        return response.as(KioskCreatePaymentResult.class);
    }

    // 결제 승인
    public static Response 결제를_승인한다(KioskConfirmPaymentRequest request) {
        return KioskClient.given()
            .body(request)
            .post("/api/payments/confirm");
    }

    public static void 결제_승인이_성공한다(Response response) {
        response.then().statusCode(HttpStatus.SC_OK);
    }
}
