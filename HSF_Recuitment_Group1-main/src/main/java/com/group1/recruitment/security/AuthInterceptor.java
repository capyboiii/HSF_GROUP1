package com.group1.recruitment.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Session-based authentication + coarse role authorization.
 *
 * Public paths are open to everyone; every other path requires a signed-in user.
 * Role-prefixed areas are further restricted. Fine-grained ownership checks
 * (e.g. an Interviewer only seeing assigned applications) live in the services.
 */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (isPublic(path)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        SessionUser user = session == null ? null : (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);

        if (user == null) {
            // Not signed in -> send to login, remembering where they wanted to go.
            String target = request.getRequestURI();
            if (request.getQueryString() != null) {
                target += "?" + request.getQueryString();
            }
            request.getSession(true).setAttribute("redirectAfterLogin", target);
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        if (!isAuthorized(path, user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have access to this page.");
            return false;
        }
        return true;
    }

    private boolean isPublic(String path) {
        return path.equals("/")
                || path.equals("/login")
                || path.equals("/logout")
                || path.equals("/register")
                || path.startsWith("/reset-password")
                || path.equals("/jobs")               // SCR-13 public job list
                || matchesPublicJobDetail(path)        // SCR-14 public job detail (GET)
                || path.startsWith("/examples")        // existing demo pages
                || path.startsWith("/css")
                || path.startsWith("/js")
                || path.startsWith("/images")
                || path.startsWith("/webjars")
                || path.startsWith("/error");
    }

    /** GET /jobs/{id} is public; POST /jobs/{id}/apply enforces auth in the controller. */
    private boolean matchesPublicJobDetail(String path) {
        return path.matches("/jobs/\\d+");
    }

    private boolean isAuthorized(String path, SessionUser user) {
        if (path.equals("/admin") || path.startsWith("/admin/")) {
            return user.isAdmin();
        }
        if (path.equals("/dashboard") || path.startsWith("/manage") || path.equals("/reports") || path.startsWith("/reports")) {
            return user.isHrOrAdmin();
        }
        if (path.equals("/my-applications") || path.startsWith("/my-applications")) {
            return user.isCandidate();
        }
        if (path.equals("/interviewer") || path.startsWith("/interviewer")) {
            return user.isInterviewer() || user.isAdmin();
        }
        // /applications/**, /profile, /change-password: any authenticated user (ownership checked in service)
        return true;
    }
}
