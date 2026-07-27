package ru.monyamau.cloudfilestorage.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

@UtilityClass
public final class CookieUtil {
    private final static int TTL_SECONDS = (60 * 30);
    private final static String COOKIE_NAME = "SESSION_ID";
    private final static String PATH = "/";

    public static ResponseCookie create(String value) {
        return ResponseCookie
                .from(COOKIE_NAME, String.valueOf(value))
                .maxAge(Duration.ofSeconds(TTL_SECONDS))
                .path(PATH)
                .build();
    }

    public static ResponseCookie delete() {
        return ResponseCookie
                .from(COOKIE_NAME)
                .maxAge(Duration.ZERO)
                .path(PATH)
                .build();
    }

    //TODO (exception)
    public static Cookie findSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(COOKIE_NAME)) {
                    return cookie;
                }
            }
        }
        throw new RuntimeException("Not find");
    }
}
