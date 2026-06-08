package com.market.backend.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyInfoUpdateRequest {

    private String email;

    private String nickname;
}
