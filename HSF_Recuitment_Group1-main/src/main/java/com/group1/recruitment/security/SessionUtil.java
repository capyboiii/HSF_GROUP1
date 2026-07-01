package com.group1.recruitment.security;

import com.group1.recruitment.exception.AccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class SessionUtil {

    private SessionUtil() {
    }

    public static SessionUser current(HttpSession session) {
        return session == null ? null : (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
    }

    /** Returns the signed-in user or throws (should not happen behind the interceptor). */
    public static SessionUser require(HttpSession session) {
        SessionUser user = current(session);
        if (user == null) {
            throw new AccessDeniedException("Not signed in.");
        }
        return user;
    }

    public static void login(HttpSession session, SessionUser user) {
        session.setAttribute(SessionUser.SESSION_KEY, user);
    }

    public static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
