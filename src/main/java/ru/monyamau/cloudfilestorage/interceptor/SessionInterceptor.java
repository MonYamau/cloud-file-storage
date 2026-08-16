package ru.monyamau.cloudfilestorage.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.monyamau.cloudfilestorage.repository.RedisSessionStorage;
import ru.monyamau.cloudfilestorage.util.CookieUtil;

@Component
public class SessionInterceptor implements HandlerInterceptor {
    private final RedisSessionStorage sessionStorage;

    public SessionInterceptor(RedisSessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Cookie currentCookie = CookieUtil.findSessionId(request).orElseThrow(() -> new RuntimeException("Не нашлась сессия"));
        String userId = sessionStorage.findBy(currentCookie.getValue()).orElseThrow(() -> new RuntimeException("Не нашёлся пользователь"));
        request.setAttribute("userId", Integer.parseInt(userId));
        return true;
    }
}
