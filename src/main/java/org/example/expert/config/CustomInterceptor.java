package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Slf4j
@Component
public class CustomInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String method = request.getMethod();
        String path = request.getRequestURI();

        boolean excludeRequest = (method.equals("DELETE") && path.matches("/admin/comments/\\d+")) ||
                (method.equals("PATCH") && path.matches("/admin/users/\\d+"));

        if (!excludeRequest) {
            return true;
        }

        // 현재 로그인한 사용자 이름을 꺼낸다.
        Long userId = (Long) request.getAttribute("userId");
        UserRole role = UserRole.of((String) request.getAttribute("userRole"));

        if (role == null || !UserRole.ADMIN.equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다.");
            return false;
        }

        log.info("[관리자 기능 접근] userId = {}, requestTime = {}, url = {}",
                                    userId, LocalDateTime.now(), path);

        return true;
    }
}
