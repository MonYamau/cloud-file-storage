package ru.monyamau.cloudfilestorage.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.monyamau.cloudfilestorage.exception.AuthenticationException;
import ru.monyamau.cloudfilestorage.repository.RedisSessionStorage;
import ru.monyamau.cloudfilestorage.util.CookieUtil;
import ru.monyamau.cloudfilestorage.util.UserContext;

@Component
public class SessionInterceptor implements HandlerInterceptor {
    private final RedisSessionStorage sessionStorage;
    private final UserContext userContext;

    public SessionInterceptor(RedisSessionStorage sessionStorage, UserContext userContext) {
        this.sessionStorage = sessionStorage;
        this.userContext = userContext;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Cookie[] cookies = request.getCookies();
        Cookie currentCookie = CookieUtil.findSessionId(cookies)
                .orElseThrow(() -> new AuthenticationException("Ошибка аутентификации: не выполнен вход пользователем"));
        String userId = sessionStorage.findBy(currentCookie.getValue())
                .orElseThrow(() -> new AuthenticationException("Ошибка аутентификации: не удалось найти актуальную сессию"));
        userContext.setUserId(Integer.valueOf(userId));
        return true;
    }
}
