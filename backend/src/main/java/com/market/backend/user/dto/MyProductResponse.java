package com.market.backend.user.dto;

import com.market.backend.product.entity.Product;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
public class MyProductResponse {

    private final Long id;

    private final String title;

    private final String imageUrl;

    private final Integer price;

    private final String description;

    private final String category;

    private final String tradeMethod;

    private final Integer locationNumber;

    private final String locationName;

    private final LocalDateTime createdAt;

    private final String createdAtText;

    private final String status;

    private MyProductResponse(
            Long id,
            String title,
            String imageUrl,
            Integer price,
            String description,
            String category,
            String tradeMethod,
            Integer locationNumber,
            String locationName,
            LocalDateTime createdAt,
            String createdAtText,
            String status
    ) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.price = price;
        this.description = description;
        this.category = category;
        this.tradeMethod = tradeMethod;
        this.locationNumber = locationNumber;
        this.locationName = locationName;
        this.createdAt = createdAt;
        this.createdAtText = createdAtText;
        this.status = status;
    }

    public static MyProductResponse from(Product product) {
        return new MyProductResponse(
                product.getId(),
                product.getProductName(),
                product.getImagePaths().isEmpty() ? null : product.getImagePaths().get(0),
                product.getPrice(),
                product.getDescription(),
                product.getCategory(),
                product.getTradeMethod(),
                product.getLocationNumber(),
                product.getLocationName(),
                product.getCreatedAt(),
                formatCreatedAtText(product.getCreatedAt()),
                "on-sale"
        );
    }

    private static String formatCreatedAtText(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "";
        }

        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        long minutes = duration.toMinutes();

        if (minutes < 1) {
            return "\uBC29\uAE08 \uC804";
        }

        if (minutes < 60) {
            return minutes + "\uBD84 \uC804";
        }

        long hours = duration.toHours();
        if (hours < 24) {
            return hours + "\uC2DC\uAC04 \uC804";
        }

        long days = duration.toDays();
        if (days < 7) {
            return days + "\uC77C \uC804";
        }

        return createdAt.toLocalDate().toString();
    }
}
