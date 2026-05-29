package com.market.backend.user.controller;

import com.market.backend.user.dto.EmailCodeVerifyRequest;
import com.market.backend.user.dto.EmailVerificationRequest;
import com.market.backend.user.dto.FindIdRequest;
import com.market.backend.user.dto.FindIdResponse;
import com.market.backend.user.dto.LoginRequest;
import com.market.backend.user.dto.MyPageResponse;
import com.market.backend.user.dto.PasswordFindCodeSendRequest;
import com.market.backend.user.dto.PasswordFindResponse;
import com.market.backend.user.dto.PasswordFindVerifyRequest;
import com.market.backend.user.dto.SignupRequest;
import com.market.backend.user.service.EmailVerificationService;
import com.market.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

        return "\uC778\uC99D\uBC88\uD638 \uBC1C\uC1A1 \uC644\uB8CC";
    }

    @PostMapping("/email/verify")
    public String verifyEmailCode(@RequestBody EmailCodeVerifyRequest request) {

        emailVerificationService.verifyCode(request.getEmail(), request.getCode());

        return "\uC774\uBA54\uC77C \uC778\uC99D \uC644\uB8CC";
    }

    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest request) {

        userService.signup(request);

        return "\uD68C\uC6D0\uAC00\uC785 \uC131\uACF5";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {

        userService.login(request);

        return "\uB85C\uADF8\uC778 \uC131\uACF5";
    }

    @PostMapping("/find-id")
    public FindIdResponse findId(@RequestBody FindIdRequest request) {

        return userService.findId(request);
    }

    @PostMapping("/password/email/send")
    public String sendPasswordFindCode(@RequestBody PasswordFindCodeSendRequest request) {

        userService.sendPasswordFindCode(request);

        return "비밀번호 찾기 인증번호 발송 완료";
    }

    @PostMapping("/password/verify")
    public PasswordFindResponse verifyPasswordFindCode(@RequestBody PasswordFindVerifyRequest request) {

        return userService.verifyPasswordFindCode(request);
    }

    @GetMapping("/me")
    public MyPageResponse getMyPage(@RequestParam String email) {

        return userService.getMyPage(email);
    }
}
