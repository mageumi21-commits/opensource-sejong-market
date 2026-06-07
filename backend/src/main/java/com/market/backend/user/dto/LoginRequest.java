package com.market.backend.user.dto;

import lombok.Getter;

@Getter
public class LoginRequest {

    private String email;

    private String studentId;

    private String password;
}
