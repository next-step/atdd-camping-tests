package com.camping.tests.scenario;

import com.camping.tests.steps.kiosk.dto.KioskProductDetail;
import io.restassured.response.Response;

import java.util.EnumMap;
import java.util.Map;

public class TestContext {
    public static void clear() {
        Product.context.clear();
        Payment.context.clear();
    }

    public static class Product {
        private static Map<Key, Object> context = new EnumMap<>(Key.class);

        private enum Key {
            상품_등록_응답,
            상품_수정_응답,
        }

        public static Response 상품_등록_응답() {
            return (Response) context.get(Key.상품_등록_응답);
        }

        public static void 상품_등록_응답(Response 상품_등록_응답) {
            context.put(Key.상품_등록_응답, 상품_등록_응답);
        }

        public static Response 상품_수정_응답() {
            return (Response) context.get(Key.상품_수정_응답);
        }

        public static void 상품_수정_응답(Response 상품_수정_응답) {
            context.put(Key.상품_수정_응답, 상품_수정_응답);
        }
    }

    public static class Payment {
        private static Map<Key, Object> context = new EnumMap<>(Key.class);

        public static enum Key {
            결제할_상품,
            결제_생성_응답,
            상품_결제_승인_응답,
        }

        public static KioskProductDetail 결제할_상품() {
            return (KioskProductDetail) context.get(Key.결제할_상품);
        }

        public static void 결제할_상품(KioskProductDetail 결제할_상품) {
            context.put(Key.결제할_상품, 결제할_상품);
        }

        public static Response 결제_생성_응답() {
            return (Response) context.get(Key.결제_생성_응답);
        }

        public static void 결제_생성_응답(Response 결제_생성_응답) {
            context.put(Key.결제_생성_응답, 결제_생성_응답);
        }

        public static Response 상품_결제_승인_응답() {
            return (Response) context.get(Key.상품_결제_승인_응답);
        }

        public static void 상품_결제_승인_응답(Response 상품_결제_승인_응답) {
            context.put(Key.상품_결제_승인_응답, 상품_결제_승인_응답);
        }
    }
}
