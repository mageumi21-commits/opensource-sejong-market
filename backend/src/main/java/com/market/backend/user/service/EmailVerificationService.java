package com.market.backend.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final String SEJONG_EMAIL_DOMAIN = "@sju.ac.kr";
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int VERIFIED_EXPIRE_MINUTES = 30;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JavaMailSender mailSender;
    private final String mailUsername;
    private final Map<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> verifiedEmails = new ConcurrentHashMap<>();

    public EmailVerificationService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String mailUsername
    ) {
        this.mailSender = mailSender;
        this.mailUsername = mailUsername;
    }

    public void sendVerificationCode(String email) {
        String normalizedEmail = normalizeSejongEmail(email);
        validateMailSettings();
        String code = createVerificationCode();

        verificationCodes.put(
                normalizedEmail,
                new VerificationCode(code, LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES))
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailUsername);
        message.setTo(normalizedEmail);
        message.setSubject("[Sejong Market] Email verification code");
        message.setText("Your Sejong Market verification code is " + code + ". It expires in 5 minutes.");

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            verificationCodes.remove(normalizedEmail);
            log.error("Failed to send verification email to {}", normalizedEmail, exception);
            throw new IllegalStateException("메일 발송에 실패했습니다. SMTP 계정과 앱 비밀번호를 확인해주세요.", exception);
        }
    }

    public void verifyCode(String email, String code) {
        String normalizedEmail = normalizeSejongEmail(email);
        VerificationCode verificationCode = verificationCodes.get(normalizedEmail);

        if (verificationCode == null || verificationCode.isExpired()) {
            verificationCodes.remove(normalizedEmail);
            throw new IllegalArgumentException("인증번호가 만료되었거나 존재하지 않습니다.");
        }

        if (code == null || !verificationCode.code().equals(code.trim())) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        verificationCodes.remove(normalizedEmail);
        verifiedEmails.put(normalizedEmail, LocalDateTime.now().plusMinutes(VERIFIED_EXPIRE_MINUTES));
    }

    public String requireVerifiedEmail(String email) {
        String normalizedEmail = normalizeSejongEmail(email);
        LocalDateTime verifiedUntil = verifiedEmails.get(normalizedEmail);

        if (verifiedUntil == null || verifiedUntil.isBefore(LocalDateTime.now())) {
            verifiedEmails.remove(normalizedEmail);
            throw new IllegalArgumentException("이메일 인증을 먼저 완료해주세요.");
        }

        return normalizedEmail;
    }

    public void consumeVerifiedEmail(String email) {
        verifiedEmails.remove(normalizeSejongEmail(email));
    }

    private String normalizeSejongEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("세종대학교 이메일만 사용할 수 있습니다.");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (!normalizedEmail.endsWith(SEJONG_EMAIL_DOMAIN)) {
            throw new IllegalArgumentException("세종대학교 이메일만 사용할 수 있습니다.");
        }

        return normalizedEmail;
    }

    private void validateMailSettings() {
        if (mailUsername == null || mailUsername.isBlank()) {
            throw new IllegalStateException("메일 발송 계정을 설정한 뒤 서버를 다시 실행해주세요.");
        }
    }

    private String createVerificationCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private record VerificationCode(String code, LocalDateTime expiresAt) {

        private boolean isExpired() {
            return expiresAt.isBefore(LocalDateTime.now());
        }
    }
}
