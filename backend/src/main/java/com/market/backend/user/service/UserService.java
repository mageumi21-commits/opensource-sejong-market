package com.market.backend.user.service;

import com.market.backend.product.entity.Product;
import com.market.backend.product.repository.ProductRepository;
import com.market.backend.user.dto.FindIdRequest;
import com.market.backend.user.dto.FindIdResponse;
import com.market.backend.user.dto.LoginRequest;
import com.market.backend.user.dto.MyPageResponse;
import com.market.backend.user.dto.PasswordFindCodeSendRequest;
import com.market.backend.user.dto.PasswordFindResponse;
import com.market.backend.user.dto.PasswordFindVerifyRequest;
import com.market.backend.user.dto.SignupRequest;
import com.market.backend.user.entity.User;
import com.market.backend.user.repository.UserRepository;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

        List<Product> products = productRepository.findBySellerOrderByCreatedAtDesc(user);

        return MyPageResponse.of(user, products);
    }

    public FindIdResponse findId(FindIdRequest request) {
        String nickname = requireText(request.getNickname(), "이름을 입력해주세요.");

        if (StringUtils.hasText(request.getStudentId())) {
            User user = userRepository.findByNicknameAndStudentId(nickname, request.getStudentId().trim())
                    .orElseThrow(() -> new IllegalArgumentException("일치하는 회원 정보를 찾을 수 없습니다."));
            return FindIdResponse.from(user);
        }

        if (StringUtils.hasText(request.getEmail())) {
            String email = normalizeSejongEmail(request.getEmail());
            User user = userRepository.findByNicknameAndEmail(nickname, email)
                    .orElseThrow(() -> new IllegalArgumentException("일치하는 회원 정보를 찾을 수 없습니다."));
            return FindIdResponse.from(user);
        }

        throw new IllegalArgumentException("학번 또는 이메일을 입력해주세요.");
    }

    public void sendPasswordFindCode(PasswordFindCodeSendRequest request) {
        User user = findUserForPassword(request.getId(), request.getNickname(), request.getEmail());
        emailVerificationService.sendVerificationCode(user.getEmail());
    }

    public PasswordFindResponse verifyPasswordFindCode(PasswordFindVerifyRequest request) {
        User user = findUserForPassword(request.getId(), request.getNickname(), request.getEmail());
        emailVerificationService.verifyCode(user.getEmail(), request.getCode());
        emailVerificationService.consumeVerifiedEmail(user.getEmail());

        return new PasswordFindResponse(user.getPassword());
    }

    private User findUserForPassword(String id, String nickname, String email) {
        String normalizedEmail = normalizeSejongEmail(email);
        String normalizedId = normalizeSejongEmail(id);
        String trimmedNickname = requireText(nickname, "이름을 입력해주세요.");

        if (!normalizedEmail.equals(normalizedId)) {
            throw new IllegalArgumentException("아이디와 이메일이 일치하지 않습니다.");
        }

        return userRepository.findByNicknameAndEmail(trimmedNickname, normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원 정보를 찾을 수 없습니다."));
    }

    private String normalizeSejongEmail(String email) {
        String value = requireText(email, "이메일을 입력해주세요.").toLowerCase(Locale.ROOT);
        if (!value.contains("@")) {
            value = value + "@sju.ac.kr";
        }
        if (!value.endsWith("@sju.ac.kr")) {
            throw new IllegalArgumentException("세종대학교 이메일만 사용할 수 있습니다.");
        }
        return value;
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
