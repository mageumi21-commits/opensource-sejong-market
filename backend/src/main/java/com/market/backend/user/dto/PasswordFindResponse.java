package com.market.backend.user.dto;

import lombok.Getter;

@Getter
public class PasswordFindResponse {

    private final String password;

    public PasswordFindResponse(String password) {
        this.password = password;
    }
}
