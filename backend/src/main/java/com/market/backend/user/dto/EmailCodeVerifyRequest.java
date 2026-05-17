package com.market.backend.user.dto;

import lombok.Getter;

@Getter
public class EmailCodeVerifyRequest {

    private String email;

    private String code;
}
