package com.market.backend.user.service;

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
}
