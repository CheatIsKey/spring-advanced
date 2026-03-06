package org.example.expert.domain.auth.service;

import org.assertj.core.api.Assertions;
import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(SpringExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtUtil jwtUtil;

    @InjectMocks
    AuthService authService;

    @Test
    @DisplayName("이미 존재하는 이메일로 가입하려는 경우, 예외 발생")
    void signup_exists_email_fail() {
        // given
        SignupRequest request = new SignupRequest("mock@test.com", "test1234", "USER");
        given(userRepository.existsByEmail(request.getEmail()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }
    
    @Test
    @DisplayName("새로운 이메일로 회원가입에 성공하면 JWT 토큰 발급")
    void signup_success() {
        // given
        SignupRequest request = new SignupRequest("mock@test.com", "test1234", "USER");

        UserRole userRole = UserRole.USER;

        User savedUser = new User("mock@test.com", "encodedPassword", userRole);
        ReflectionTestUtils.setField(savedUser, "id", 1L);

        given(userRepository.existsByEmail(request.getEmail()))
                .willReturn(false);

        given(passwordEncoder.encode(request.getPassword()))
                .willReturn("encodedPassword");

        given(userRepository.save(any(User.class)))
                .willReturn(savedUser);

        given(jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getUserRole()))
                .willReturn("Bearer Token");

        // when
        SignupResponse response = authService.signup(request);

        // then
        assertThat(response.getBearerToken()).isEqualTo("Bearer Token");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).createToken(1L, "mock@test.com", userRole);
    }

    @Test
    @DisplayName("가입하지 않은 사용자의 로그인 요청은 예외 발생")
    void signin_email_not_found_fail() {
        // given
        SigninRequest request = new SigninRequest("mock@test.com", "test1234");

        given(userRepository.findByEmail(request.getEmail()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.signin(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("가입되지 않은 유저입니다.");
    }

    @Test
    @DisplayName("가입한 이메일의 비밀번호와 다르면 예외 발생")
    void signin_password_mismatch_fail() {
        // given
        User user = new User("mock@test.com", "test1234", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userRepository.findByEmail(any()))
                .willReturn(Optional.of(user));

        SigninRequest request = new SigninRequest("mock@test.com", "test");

        given(passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.signin(request))
                .isInstanceOf(AuthException.class)
                .hasMessage("잘못된 비밀번호입니다.");
    }

    @Test
    @DisplayName("로그인에 성공하면 JWT 토큰 발급")
    void signin_generate_token_success() {
        // given
        User user = new User("mock@test.com", "test1234", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userRepository.findByEmail(any()))
                .willReturn(Optional.of(user));

        SigninRequest request = new SigninRequest("mock@test.com", "test1234");

        given(passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .willReturn(true);

        given(jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole()))
                .willReturn("Bearer Token");

        // when
        SigninResponse response = authService.signin(request);

        // then
        assertThat(response.getBearerToken()).isEqualTo("Bearer Token");
        verify(jwtUtil).createToken(1L, "mock@test.com", UserRole.USER);
    }
}