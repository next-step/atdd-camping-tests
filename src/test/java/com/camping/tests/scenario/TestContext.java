package com.camping.tests.scenario;

import com.camping.tests.steps.kiosk.dto.KioskProductDetail;
import io.restassured.response.Response;

import java.util.EnumMap;
import java.util.Map;

public class TestContext {
    public static void clear() {
        Product.context.clear();
        Payment.context.clear();
        Rental.context.clear();
        Reservation.context.clear();
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

    public static class Reservation {
        private static Map<Key, Object> context = new EnumMap<>(Key.class);

        private enum Key {
            예약_생성_응답,
            예약_ID,
            예약_상태_변경_응답,
        }

        public static Response 예약_생성_응답() {
            return (Response) context.get(Key.예약_생성_응답);
        }

        public static void 예약_생성_응답(Response 예약_생성_응답) {
            context.put(Key.예약_생성_응답, 예약_생성_응답);
        }

        public static Long 예약_ID() {
            return (Long) context.get(Key.예약_ID);
        }

        public static void 예약_ID(Long 예약_ID) {
            context.put(Key.예약_ID, 예약_ID);
        }

        public static Response 예약_상태_변경_응답() {
            return (Response) context.get(Key.예약_상태_변경_응답);
        }

        public static void 예약_상태_변경_응답(Response 예약_상태_변경_응답) {
            context.put(Key.예약_상태_변경_응답, 예약_상태_변경_응답);
        }
    }

    public static class Rental {
        private static Map<Key, Object> context = new EnumMap<>(Key.class);

        private enum Key {
            렌탈_등록_응답,
            렌탈_반납_응답,
            렌탈_ID,
            상품_ID,
        }

        public static Response 렌탈_등록_응답() {
            return (Response) context.get(Key.렌탈_등록_응답);
        }

        public static void 렌탈_등록_응답(Response 렌탈_등록_응답) {
            context.put(Key.렌탈_등록_응답, 렌탈_등록_응답);
        }

        public static Response 렌탈_반납_응답() {
            return (Response) context.get(Key.렌탈_반납_응답);
        }

        public static void 렌탈_반납_응답(Response 렌탈_반납_응답) {
            context.put(Key.렌탈_반납_응답, 렌탈_반납_응답);
        }

        public static Long 렌탈_ID() {
            return (Long) context.get(Key.렌탈_ID);
        }

        public static void 렌탈_ID(Long 렌탈_ID) {
            context.put(Key.렌탈_ID, 렌탈_ID);
        }

        public static Long 상품_ID() {
            return (Long) context.get(Key.상품_ID);
        }

        public static void 상품_ID(Long 상품_ID) {
            context.put(Key.상품_ID, 상품_ID);
        }
    }
}
