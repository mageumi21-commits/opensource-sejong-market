package com.market.backend.user.dto;

import lombok.Getter;

@Getter
public class SignupRequest {

    private String email;

    private String password;

    private String nickname;

    private String studentId;
}