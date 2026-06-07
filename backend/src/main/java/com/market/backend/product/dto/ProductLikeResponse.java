package com.market.backend.product.dto;

public class ProductLikeResponse {

    private final Long productId;
    private final boolean liked;

    private ProductLikeResponse(Long productId, boolean liked) {
        this.productId = productId;
        this.liked = liked;
    }

    public static ProductLikeResponse of(Long productId, boolean liked) {
        return new ProductLikeResponse(productId, liked);
    }

    public Long getProductId() {
        return productId;
    }

    public boolean isLiked() {
        return liked;
    }
}
