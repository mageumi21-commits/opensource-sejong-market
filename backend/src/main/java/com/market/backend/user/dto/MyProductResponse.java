package com.market.backend.user.dto;

import com.market.backend.product.entity.Product;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MyProductResponse {

    private final String title;

    private final String imageUrl;

    private final Integer price;

    private final String description;

    private final LocalDateTime createdAt;

    private MyProductResponse(String title, String imageUrl, Integer price, String description, LocalDateTime createdAt) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.price = price;
        this.description = description;
        this.createdAt = createdAt;
    }

    public static MyProductResponse from(Product product) {
        return new MyProductResponse(
                product.getTitle(),
                product.getImageUrl(),
                product.getPrice(),
                product.getDescription(),
                product.getCreatedAt()
        );
    }
}
