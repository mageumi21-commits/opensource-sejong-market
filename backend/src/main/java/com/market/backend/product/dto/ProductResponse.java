package com.market.backend.product.dto;

import com.market.backend.product.entity.Product;
import com.market.backend.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class ProductResponse {

    private final Long id;
    private final Long sellerId;
    private final String sellerEmail;
    private final String sellerNickname;
    private final String productName;
    private final String category;
    private final Integer price;
    private final String description;
    private final String tradeMethod;
    private final Integer locationNumber;
    private final String locationName;
    private final List<String> imagePaths;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ProductResponse(Product product) {
        User seller = product.getSeller();

        this.id = product.getId();
        this.sellerId = seller == null ? null : seller.getId();
        this.sellerEmail = seller == null ? null : seller.getEmail();
        this.sellerNickname = seller == null ? null : seller.getNickname();
        this.productName = product.getProductName();
        this.category = product.getCategory();
        this.price = product.getPrice();
        this.description = product.getDescription();
        this.tradeMethod = product.getTradeMethod();
        this.locationNumber = product.getLocationNumber();
        this.locationName = product.getLocationName();
        this.imagePaths = product.getImagePaths();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
    }

    public static ProductResponse from(Product product) {
        return new ProductResponse(product);
    }
}
