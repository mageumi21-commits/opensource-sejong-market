package com.market.backend.user.service;

import com.market.backend.user.dto.LoginRequest;
import com.market.backend.user.dto.MyPageResponse;
import com.market.backend.user.dto.SignupRequest;
import com.market.backend.user.entity.User;
import com.market.backend.user.repository.UserRepository;
import com.market.backend.product.entity.Product;
import com.market.backend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final ProductRepository productRepository;

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

    public MyPageResponse getMyPage(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("\uC874\uC7AC\uD558\uC9C0 \uC54A\uB294 \uC0AC\uC6A9\uC790\uC785\uB2C8\uB2E4."));

        List<Product> products = productRepository.findByUserOrderByCreatedAtDesc(user);

        return MyPageResponse.of(user, products);
    }
}
