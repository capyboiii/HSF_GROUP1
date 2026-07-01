package com.group1.recruitment.security;

import com.group1.recruitment.entity.User;

import java.io.Serializable;

/**
 * Lightweight snapshot of the authenticated user kept in the HTTP session.
 * Session key is {@link #SESSION_KEY}.
 */
public class SessionUser implements Serializable {

    public static final String SESSION_KEY = "currentUser";

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_HR = "ROLE_HR";
    public static final String ROLE_INTERVIEWER = "ROLE_INTERVIEWER";
    public static final String ROLE_CANDIDATE = "ROLE_CANDIDATE";

    private final Long id;
    private final String fullName;
    private final String username;
    private final String email;
    private final String roleName;

    public SessionUser(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.roleName = user.getRole() != null ? user.getRole().getName() : null;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRoleName() { return roleName; }

    public boolean isAdmin() { return ROLE_ADMIN.equals(roleName); }
    public boolean isHr() { return ROLE_HR.equals(roleName); }
    public boolean isInterviewer() { return ROLE_INTERVIEWER.equals(roleName); }
    public boolean isCandidate() { return ROLE_CANDIDATE.equals(roleName); }

    /** HR Manager screens are also accessible to Admin (Admin inherits HR permissions). */
    public boolean isHrOrAdmin() { return isHr() || isAdmin(); }

    /** Landing page after sign-in, per SCR-01 navigation rules. */
    public String homePath() {
        if (isAdmin()) return "/admin";
        if (isHr()) return "/dashboard";
        if (isInterviewer()) return "/interviewer";
        return "/my-applications";
    }

    /** Human-readable role label for the UI. */
    public String roleLabel() {
        if (isAdmin()) return "Admin";
        if (isHr()) return "HR Manager";
        if (isInterviewer()) return "Interviewer";
        if (isCandidate()) return "Candidate";
        return roleName;
    }
}
