package com.market.backend.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductUpdateRequest {

    private String sellerEmail;
    private String productName;
    private String category;
    private Integer price;
    private String description;
    private String tradeMethod;
    private Integer locationNumber;
    private String locationName;
}
