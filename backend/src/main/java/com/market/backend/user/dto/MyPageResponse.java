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

    private MyPageResponse(String email, String nickname, String studentId, List<MyProductResponse> products) {
        this.email = email;
        this.nickname = nickname;
        this.studentId = studentId;
        this.products = products;
    }

    public static MyPageResponse of(User user, List<Product> products) {
        return new MyPageResponse(
                user.getEmail(),
                user.getNickname(),
                user.getStudentId(),
                products.stream()
                        .map(MyProductResponse::from)
                        .toList()
        );
    }
}
