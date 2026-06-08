package com.market.backend.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordUpdateRequest {

    private String email;

    private String currentPassword;

    private String newPassword;
}
