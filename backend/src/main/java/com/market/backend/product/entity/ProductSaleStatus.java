package com.market.backend.product.entity;

public enum ProductSaleStatus {

    ON_SALE("판매중", "on-sale"),
    SOLD_OUT("판매완료", "sold-out");

    private final String text;
    private final String clientValue;

    ProductSaleStatus(String text, String clientValue) {
        this.text = text;
        this.clientValue = clientValue;
    }

    public String getText() {
        return text;
    }

    public String getClientValue() {
        return clientValue;
    }
}
