package com.market.backend.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordFindCodeSendRequest {

    private String id;
    private String nickname;
    private String email;
}
