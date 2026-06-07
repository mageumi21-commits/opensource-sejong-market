package com.market.backend.user.dto;

import com.market.backend.product.entity.Product;
import com.market.backend.user.entity.User;
import lombok.Getter;

import java.util.List;

@Getter
public class MyPageResponse {

    private final String email;

    private final String nickname;

    private final String studentId;

    private final List<MyProductResponse> products;

    private final List<MyProductResponse> likedProducts;

    private MyPageResponse(
            String email,
            String nickname,
            String studentId,
            List<MyProductResponse> products,
            List<MyProductResponse> likedProducts
    ) {
        this.email = email;
        this.nickname = nickname;
        this.studentId = studentId;
        this.products = products;
        this.likedProducts = likedProducts;
    }

    public static MyPageResponse of(User user, List<Product> products) {
        return of(user, products, List.of());
    }

    public static MyPageResponse of(User user, List<Product> products, List<Product> likedProducts) {
        return new MyPageResponse(
                user.getEmail(),
                user.getNickname(),
                user.getStudentId(),
                products.stream()
                        .map(MyProductResponse::from)
                        .toList(),
                likedProducts.stream()
                        .map(MyProductResponse::from)
                        .toList()
        );
    }
}
