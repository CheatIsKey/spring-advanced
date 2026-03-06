package org.example.expert.config;

import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.NativeWebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthUserArgumentResolverTest {

    @InjectMocks
    AuthUserArgumentResolver resolver;

    void dummyMethod(@Auth AuthUser authUser){}
    void dummyMethodWithoutAuthAndType(String authUser){}
    void dummyMethodWithWrongTypeAuth(@Auth String authUser){}

    @Test
    @DisplayName("@Auth가 사용된 메서드가 있고 AuthUser 타입이면 true 반환")
    void support_parameter_success() throws NoSuchMethodException {
        //given
        MethodParameter dummyMethod = new MethodParameter(
                this.getClass().getDeclaredMethod("dummyMethod", AuthUser.class), 0
        );

        //when
        boolean result = resolver.supportsParameter(dummyMethod);

        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("@Auth가 사용된 메서드가 없고 AuthUser 타입도 아니면 false 반환")
    void support_parameter_not_use_Auth_param_and_wrong_type_fail() throws NoSuchMethodException {
        //given
        MethodParameter dummyMethod = new MethodParameter(
                this.getClass().getDeclaredMethod("dummyMethodWithoutAuthAndType", String.class), 0
        );

        //when
        boolean result = resolver.supportsParameter(dummyMethod);

        //then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("@Auth가 사용된 메서드가 있지만, AuthUser 타입이 아니면 예외 발생")
    void support_parameter_wrong_type_fail() throws NoSuchMethodException {
        //given
        MethodParameter dummyMethod = new MethodParameter(
                this.getClass().getDeclaredMethod("dummyMethodWithWrongTypeAuth", String.class), 0
        );

        //when & then
        assertThatThrownBy(() -> resolver.supportsParameter(dummyMethod))
                .isInstanceOf(AuthException.class)
                .hasMessage("@Auth와 AuthUser 타입은 함께 사용되어야 합니다.");
    }

    @Test
    @DisplayName("request에서 정보를 추출해서 AuthUser로 조립 후 반환")
    void resolve_argument_success() {
        //given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setAttribute("userId", 1L);
        mockRequest.setAttribute("email", "mock@test.com");
        mockRequest.setAttribute("userRole", "USER");

        NativeWebRequest request = mock(NativeWebRequest.class);
        given(request.getNativeRequest()).willReturn(mockRequest);

        //when
        AuthUser result = (AuthUser) resolver.resolveArgument(null, null, request, null);

        //then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("mock@test.com");
        assertThat(result.getUserRole()).isEqualTo(UserRole.USER);
    }
}