package com.market.backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.market.backend.product.repository.ProductRepository;
import com.market.backend.user.dto.FindIdRequest;
import com.market.backend.user.dto.FindIdResponse;
import com.market.backend.user.dto.LoginRequest;
import com.market.backend.user.dto.LoginResponse;
import com.market.backend.user.dto.PasswordFindCodeSendRequest;
import com.market.backend.user.dto.PasswordFindResponse;
import com.market.backend.user.dto.PasswordFindVerifyRequest;
import com.market.backend.user.dto.SignupRequest;
import com.market.backend.user.entity.User;
import com.market.backend.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void signup_savesVerifiedUser() {
        SignupRequest request = signupRequest("student@sju.ac.kr", "password123", "테스트유저", "23011234");
        when(emailVerificationService.requireVerifiedEmail("student@sju.ac.kr")).thenReturn("student@sju.ac.kr");

        userService.signup(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(emailVerificationService).consumeVerifiedEmail("student@sju.ac.kr");

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("student@sju.ac.kr");
        assertThat(savedUser.getPassword()).isEqualTo("password123");
        assertThat(savedUser.getNickname()).isEqualTo("테스트유저");
        assertThat(savedUser.getStudentId()).isEqualTo("23011234");
    }

    @Test
    void signup_rejectsUnverifiedEmail() {
        SignupRequest request = signupRequest("student@sju.ac.kr", "password123", "테스트유저", "23011234");
        when(emailVerificationService.requireVerifiedEmail("student@sju.ac.kr"))
                .thenThrow(new IllegalArgumentException("이메일 인증을 먼저 완료해주세요."));

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 인증을 먼저 완료해주세요.");
    }

    @Test
    void signup_rejectsDuplicateEmail() {
        SignupRequest request = signupRequest("student@sju.ac.kr", "password123", "테스트유저", "23011234");
        User user = user("student@sju.ac.kr", "password123", "테스트유저", "23011234");
        when(emailVerificationService.requireVerifiedEmail("student@sju.ac.kr")).thenReturn("student@sju.ac.kr");
        when(userRepository.findByEmail("student@sju.ac.kr")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 가입된 이메일입니다.");
    }

    @Test
    void login_acceptsCorrectPassword() {
        LoginRequest request = loginRequest("student@sju.ac.kr", "password123");
        when(userRepository.findByEmail("student@sju.ac.kr"))
                .thenReturn(Optional.of(user("student@sju.ac.kr", "password123", "테스트유저", "23011234")));

        LoginResponse response = userService.login(request);

        assertThat(response.getEmail()).isEqualTo("student@sju.ac.kr");
        assertThat(response.getNickname()).isEqualTo("테스트유저");
        assertThat(response.getStudentId()).isEqualTo("23011234");
        verify(userRepository).findByEmail("student@sju.ac.kr");
    }

    @Test
    void login_acceptsStudentIdWithCorrectPassword() {
        LoginRequest request = studentLoginRequest("23011234", "password123");
        when(userRepository.findByStudentId("23011234"))
                .thenReturn(Optional.of(user("student@sju.ac.kr", "password123", "테스트유저", "23011234")));

        LoginResponse response = userService.login(request);

        assertThat(response.getEmail()).isEqualTo("student@sju.ac.kr");
        assertThat(response.getNickname()).isEqualTo("테스트유저");
        assertThat(response.getStudentId()).isEqualTo("23011234");
        verify(userRepository).findByStudentId("23011234");
    }

    @Test
    void login_rejectsUnknownEmail() {
        LoginRequest request = loginRequest("unknown@sju.ac.kr", "password123");
        when(userRepository.findByEmail("unknown@sju.ac.kr")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    @Test
    void login_rejectsWrongPassword() {
        LoginRequest request = loginRequest("student@sju.ac.kr", "wrong");
        when(userRepository.findByEmail("student@sju.ac.kr"))
                .thenReturn(Optional.of(user("student@sju.ac.kr", "password123", "테스트유저", "23011234")));

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    @Test
    void login_rejectsUnknownStudentId() {
        LoginRequest request = studentLoginRequest("99999999", "password123");
        when(userRepository.findByStudentId("99999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    @Test
    void findId_returnsMaskedEmailByStudentId() {
        FindIdRequest request = findIdRequest("테스트유저", "23011234", null);
        User user = user("student@sju.ac.kr", "password123", "테스트유저", "23011234");
        when(userRepository.findByNicknameAndStudentId("테스트유저", "23011234")).thenReturn(Optional.of(user));

        FindIdResponse response = userService.findId(request);

        assertThat(response.getEmail()).isEqualTo("student@sju.ac.kr");
        assertThat(response.getMaskedEmail()).isEqualTo("st****@sju.ac.kr");
    }

    @Test
    void findId_returnsMaskedEmailByEmailId() {
        FindIdRequest request = findIdRequest("테스트유저", null, "student");
        User user = user("student@sju.ac.kr", "password123", "테스트유저", "23011234");
        when(userRepository.findByNicknameAndEmail("테스트유저", "student@sju.ac.kr")).thenReturn(Optional.of(user));

        FindIdResponse response = userService.findId(request);

        assertThat(response.getEmail()).isEqualTo("student@sju.ac.kr");
    }

    @Test
    void findId_rejectsMissingIdentifier() {
        FindIdRequest request = findIdRequest("테스트유저", null, null);

        assertThatThrownBy(() -> userService.findId(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("학번 또는 이메일을 입력해주세요.");
    }

    @Test
    void sendPasswordFindCode_sendsCodeToMatchedUserEmail() {
        PasswordFindCodeSendRequest request = passwordCodeSendRequest("student", "테스트유저", "student@sju.ac.kr");
        User user = user("student@sju.ac.kr", "password123", "테스트유저", "23011234");
        when(userRepository.findByNicknameAndEmail("테스트유저", "student@sju.ac.kr")).thenReturn(Optional.of(user));

        userService.sendPasswordFindCode(request);

        verify(emailVerificationService).sendVerificationCode("student@sju.ac.kr");
    }

    @Test
    void sendPasswordFindCode_rejectsMismatchedIdAndEmail() {
        PasswordFindCodeSendRequest request = passwordCodeSendRequest("other", "테스트유저", "student@sju.ac.kr");

        assertThatThrownBy(() -> userService.sendPasswordFindCode(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("아이디와 이메일이 일치하지 않습니다.");
    }

    @Test
    void verifyPasswordFindCode_returnsPassword() {
        PasswordFindVerifyRequest request = passwordVerifyRequest("student", "테스트유저", "student@sju.ac.kr", "123456");
        User user = user("student@sju.ac.kr", "password123", "테스트유저", "23011234");
        when(userRepository.findByNicknameAndEmail("테스트유저", "student@sju.ac.kr")).thenReturn(Optional.of(user));

        PasswordFindResponse response = userService.verifyPasswordFindCode(request);

        verify(emailVerificationService).verifyCode("student@sju.ac.kr", "123456");
        verify(emailVerificationService).consumeVerifiedEmail("student@sju.ac.kr");
        assertThat(response.getPassword()).isEqualTo("password123");
    }

    @Test
    void getMyPage_returnsUserInformation() {
        User user = user("student@sju.ac.kr", "password123", "테스트유저", "23011234");
        when(userRepository.findByEmail("student@sju.ac.kr")).thenReturn(Optional.of(user));
        when(productRepository.findBySellerOrderByCreatedAtDesc(user)).thenReturn(List.of());

        var response = userService.getMyPage("student@sju.ac.kr");

        assertThat(response.getEmail()).isEqualTo("student@sju.ac.kr");
        assertThat(response.getNickname()).isEqualTo("테스트유저");
        assertThat(response.getStudentId()).isEqualTo("23011234");
        assertThat(response.getProducts()).isEmpty();
    }

    private SignupRequest signupRequest(String email, String password, String nickname, String studentId) {
        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "password", password);
        ReflectionTestUtils.setField(request, "nickname", nickname);
        ReflectionTestUtils.setField(request, "studentId", studentId);
        return request;
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "password", password);
        return request;
    }

    private LoginRequest studentLoginRequest(String studentId, String password) {
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "studentId", studentId);
        ReflectionTestUtils.setField(request, "password", password);
        return request;
    }

    private FindIdRequest findIdRequest(String nickname, String studentId, String email) {
        FindIdRequest request = new FindIdRequest();
        ReflectionTestUtils.setField(request, "nickname", nickname);
        ReflectionTestUtils.setField(request, "studentId", studentId);
        ReflectionTestUtils.setField(request, "email", email);
        return request;
    }

    private PasswordFindCodeSendRequest passwordCodeSendRequest(String id, String nickname, String email) {
        PasswordFindCodeSendRequest request = new PasswordFindCodeSendRequest();
        ReflectionTestUtils.setField(request, "id", id);
        ReflectionTestUtils.setField(request, "nickname", nickname);
        ReflectionTestUtils.setField(request, "email", email);
        return request;
    }

    private PasswordFindVerifyRequest passwordVerifyRequest(String id, String nickname, String email, String code) {
        PasswordFindVerifyRequest request = new PasswordFindVerifyRequest();
        ReflectionTestUtils.setField(request, "id", id);
        ReflectionTestUtils.setField(request, "nickname", nickname);
        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "code", code);
        return request;
    }

    private User user(String email, String password, String nickname, String studentId) {
        return new User(email, password, nickname, studentId);
    }
}
