package org.example.expert.config;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CustomInterceptorTest {

    CustomInterceptor customInterceptor = new CustomInterceptor();

    @Test
    @DisplayName("Admin 경로가 아니면 바로 true 반환")
    void preHandle_not_admin_path_success() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/todos");
        MockHttpServletResponse response = new MockHttpServletResponse();

        //when
        boolean result = customInterceptor.preHandle(request, response, new Object());

        //then
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("DELETE /admin/comments/{commentId} 요청에 ADMIN 권한이면 true 반환")
    void preHandle_admin_delete_comment_success() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/admin/comments/1");
        request.setAttribute("commentId", 1L);
        request.setAttribute("userRole", "ADMIN");
        MockHttpServletResponse response = new MockHttpServletResponse();

        //when
        boolean result = customInterceptor.preHandle(request, response, new Object());

        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("DELETE /admin/comments/{commentId} 요청에 ADMIN 권한이 아니면 false 반환")
    void preHandle_admin_delete_comment_UserRole_fail() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/admin/comments/1");
        request.setAttribute("userId", 1L);
        request.setAttribute("userRole", "USER");
        MockHttpServletResponse response = new MockHttpServletResponse();

        //when
        boolean result = customInterceptor.preHandle(request, response, new Object());

        //then
        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("PATCH /admin/users/{userId} 요청에 ADMIN 권한이면 true 반환")
    void preHandle_admin_patch_user_true() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/admin/users/1");
        request.setAttribute("userId", 1L);
        request.setAttribute("userRole", "ADMIN");
        MockHttpServletResponse response = new MockHttpServletResponse();

        //when
        boolean result = customInterceptor.preHandle(request, response, new Object());

        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("PATCH /admin/users/{userId} 요청에 ADMIN 권한이 아니면 예외 발생")
    void preHandle_admin_patch_userRole_is_null_fail() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/admin/users/1");
        request.setAttribute("userId", 1L);
        request.setAttribute("userRole", null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        //when
        boolean result = customInterceptor.preHandle(request, response, new Object());

        //then
        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("PATCH /admin/users/{userId} 요청에 ADMIN 권한이 아니면 예외 발생")
    void preHandle_admin_patch_user_AdminRole_fail() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/admin/users/1");
        request.setAttribute("userId", 1L);
        request.setAttribute("userRole", "USER");
        MockHttpServletResponse response = new MockHttpServletResponse();

        //when
        boolean result = customInterceptor.preHandle(request, response, new Object());

        //then
        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
    }
}