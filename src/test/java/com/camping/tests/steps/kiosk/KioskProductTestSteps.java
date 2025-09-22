package com.camping.tests.steps.kiosk;

import static org.assertj.core.api.Assertions.assertThat;

import com.camping.tests.steps.kiosk.dto.KioskProductDetail;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import java.util.List;
import org.apache.http.HttpStatus;

public class KioskProductTestSteps {
    // 상품 목록 조회
    public static Response 상품_목록에서_상품이_조회된다(String targetName) {
        var 상품_목록_조회_응답 = 상품_목록을_조회한다();
        상품_목록_조회가_성공한다(상품_목록_조회_응답);
        상품_목록에_상품이_있다(상품_목록_조회_응답, targetName);
        return 상품_목록_조회_응답;
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

    public static void 상품_목록에_상품이_있다(Response response, String targetName) {
        var productDetails = response.as(new TypeRef<List<KioskProductDetail>>() {
        });
        assertThat(productDetails)
            .extracting(KioskProductDetail::name)
            .contains(targetName);
    }

    public static void 상품_목록에_상품이_없다(Response response, String targetName) {
        var productDetails = response.as(new TypeRef<List<KioskProductDetail>>() {
        });
        assertThat(productDetails)
            .extracting(KioskProductDetail::name)
            .doesNotContain(targetName);
    }
}
