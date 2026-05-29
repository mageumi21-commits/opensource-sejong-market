package com.market.backend.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FindIdRequest {

    private String nickname;
    private String studentId;
    private String email;
}
