package com.market.backend.product.dto;

import com.market.backend.product.entity.Product;
import com.market.backend.user.entity.User;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class ProductListResponse {

    private final Long id;
    private final Long sellerId;
    private final String sellerEmail;
    private final String sellerNickname;
    private final String productName;
    private final String name;
    private final String category;
    private final String imageUrl;
    private final Integer price;
    private final String description;
    private final String locationName;
    private final LocalDateTime createdAt;
    private final String createdAtText;
    private final String badgeText;
    private final boolean liked;

    private ProductListResponse(Product product) {
        User seller = product.getSeller();
        List<String> imagePaths = product.getImagePaths();

        this.id = product.getId();
        this.sellerId = seller == null ? null : seller.getId();
        this.sellerEmail = seller == null ? null : seller.getEmail();
        this.sellerNickname = seller == null ? null : seller.getNickname();
        this.productName = product.getProductName();
        this.name = product.getProductName();
        this.category = product.getCategory();
        this.imageUrl = imagePaths == null || imagePaths.isEmpty() ? null : imagePaths.get(0);
        this.price = product.getPrice();
        this.description = product.getDescription();
        this.locationName = product.getLocationName();
        this.createdAt = product.getCreatedAt();
        this.createdAtText = formatCreatedAtText(product.getCreatedAt());
        this.badgeText = "";
        this.liked = false;
    }

    public static ProductListResponse from(Product product) {
        return new ProductListResponse(product);
    }

    private String formatCreatedAtText(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "";
        }

        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        long minutes = duration.toMinutes();

        if (minutes < 1) {
            return "방금 전";
        }

        if (minutes < 60) {
            return minutes + "분 전";
        }

        long hours = duration.toHours();
        if (hours < 24) {
            return hours + "시간 전";
        }

        long days = duration.toDays();
        if (days < 7) {
            return days + "일 전";
        }

        return createdAt.toLocalDate().toString();
    }
}
