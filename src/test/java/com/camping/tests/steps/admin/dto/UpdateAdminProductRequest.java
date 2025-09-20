package com.camping.tests.steps.admin.dto;

import lombok.Builder;
import lombok.Setter;
import lombok.experimental.Accessors;

@Builder
public record UpdateAdminProductRequest(
    String name,
    String description,
    AdminProductType productType,
    int price,
    int stockQuantity
) {

    public static Fixture fixture() {
        return new Fixture();
    }

    @Setter
    @Accessors(fluent = true)
    public static class Fixture {
        private String name = "텐트";
        private String description = "4인용 텐트";
        private AdminProductType productType = AdminProductType.RENTAL;
        private int price = 200_000;
        private int stockQuantity = 10;

        public UpdateAdminProductRequest create() {
            return UpdateAdminProductRequest.builder()
                .name(name)
                .description(description)
                .productType(productType)
                .price(price)
                .stockQuantity(stockQuantity)
                .build();
        }
    }
}
