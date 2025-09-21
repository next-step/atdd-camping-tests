package com.camping.tests.steps.kiosk.dto;

import java.util.List;
import lombok.Builder;
import lombok.Setter;
import lombok.experimental.Accessors;

@Builder
public record KioskCreatePaymentRequest(
    List<Item> items,
    String paymentMethod
) {
    public static Fixture fixture() {
        return new Fixture();
    }

    @Setter
    @Accessors(fluent = true)
    public static class Fixture {
        private List<Item> items = List.of(Item.fixture().create());
        private String paymentMethod = "CARD";

        public KioskCreatePaymentRequest create() {
            return KioskCreatePaymentRequest.builder()
                .items(items)
                .paymentMethod(paymentMethod)
                .build();
        }
    }

    @Builder
    public record Item(
        long productId,
        String productName,
        int unitPrice,
        int quantity
    ) {
        public static Fixture fixture() {
            return new Fixture();
        }

        @Setter
        @Accessors(fluent = true)
        public static class Fixture {
            private long productId = 1;
            private String productName = "텐트";
            private int unitPrice = 200_000;
            private int quantity = 2;

            public Item create() {
                return Item.builder()
                    .productId(productId)
                    .productName(productName)
                    .unitPrice(unitPrice)
                    .quantity(quantity)
                    .build();
            }
        }
    }
}

