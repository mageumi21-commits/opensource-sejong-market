package com.market.backend.user.dto;

import com.market.backend.user.entity.User;
import lombok.Getter;

@Getter
public class FindIdResponse {

    private final String email;
    private final String maskedEmail;

    private FindIdResponse(User user) {
        this.email = user.getEmail();
        this.maskedEmail = maskEmail(user.getEmail());
    }

    public static FindIdResponse from(User user) {
        return new FindIdResponse(user);
    }

    private static String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) {
            return "****" + email.substring(atIndex);
        }

        String id = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        return id.substring(0, 2) + "****" + domain;
    }
}
