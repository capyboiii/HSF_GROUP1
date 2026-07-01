package com.group1.recruitment.controller;

import com.group1.recruitment.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Exposes the signed-in user to every view as {@code currentUser} so the shared
 * layout (sidebar/header) can render role-aware navigation.
 */
@ControllerAdvice(basePackages = "com.group1.recruitment.controller")
public class GlobalModelAdvice {

    @ModelAttribute("currentUser")
    public SessionUser currentUser(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
    }
}
