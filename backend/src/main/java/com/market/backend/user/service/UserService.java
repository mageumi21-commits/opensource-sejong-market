package com.market.backend.user.service;

import com.market.backend.user.dto.LoginRequest;
import com.market.backend.user.dto.SignupRequest;
import com.market.backend.user.entity.User;
import com.market.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;

    public void signup(SignupRequest request) {
        String email = emailVerificationService.requireVerifiedEmail(request.getEmail());

        User user = new User(
                email,
                request.getPassword(),
                request.getNickname(),
                request.getStudentId()
        );

        userRepository.save(user);
        emailVerificationService.consumeVerifiedEmail(email);
    }

    public void login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("\uC774\uBA54\uC77C \uB610\uB294 \uBE44\uBC00\uBC88\uD638\uAC00 \uC77C\uCE58\uD558\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4."));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("\uC774\uBA54\uC77C \uB610\uB294 \uBE44\uBC00\uBC88\uD638\uAC00 \uC77C\uCE58\uD558\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4.");
        }
    }
}
