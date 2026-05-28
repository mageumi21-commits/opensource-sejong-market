package com.market.backend.product.dto;

import com.market.backend.product.entity.Product;
import com.market.backend.user.entity.User;
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
    private final String imageUrl;
    private final Integer price;
    private final String description;
    private final LocalDateTime createdAt;

    private ProductListResponse(Product product) {
        User seller = product.getSeller();
        List<String> imagePaths = product.getImagePaths();

        this.id = product.getId();
        this.sellerId = seller == null ? null : seller.getId();
        this.sellerEmail = seller == null ? null : seller.getEmail();
        this.sellerNickname = seller == null ? null : seller.getNickname();
        this.productName = product.getProductName();
        this.imageUrl = imagePaths == null || imagePaths.isEmpty() ? null : imagePaths.get(0);
        this.price = product.getPrice();
        this.description = product.getDescription();
        this.createdAt = product.getCreatedAt();
    }

    public static ProductListResponse from(Product product) {
        return new ProductListResponse(product);
    }
}
