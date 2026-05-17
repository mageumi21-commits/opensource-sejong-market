package com.market.backend.user.controller;

import com.market.backend.user.dto.EmailCodeVerifyRequest;
import com.market.backend.user.dto.EmailVerificationRequest;
import com.market.backend.user.dto.SignupRequest;
import com.market.backend.user.service.EmailVerificationService;
import com.market.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/email/send")
    public String sendEmailVerificationCode(@RequestBody EmailVerificationRequest request) {

        emailVerificationService.sendVerificationCode(request.getEmail());

        return "인증번호 발송 완료";
    }

    @PostMapping("/email/verify")
    public String verifyEmailCode(@RequestBody EmailCodeVerifyRequest request) {

        emailVerificationService.verifyCode(request.getEmail(), request.getCode());

        return "이메일 인증 완료";
    }

    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest request) {

        userService.signup(request);

        return "회원가입 성공";
    }
}
