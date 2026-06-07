package com.market.backend.user.dto;

import com.market.backend.user.entity.User;
import lombok.Getter;

@Getter
public class LoginResponse {

    private final String email;

    private final String nickname;

    private final String studentId;

    private LoginResponse(String email, String nickname, String studentId) {
        this.email = email;
        this.nickname = nickname;
        this.studentId = studentId;
    }

    public static LoginResponse from(User user) {
        return new LoginResponse(
                user.getEmail(),
                user.getNickname(),
                user.getStudentId()
        );
    }
}
