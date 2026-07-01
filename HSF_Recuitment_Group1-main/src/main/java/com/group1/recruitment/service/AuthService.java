package com.group1.recruitment.service;

import com.group1.recruitment.entity.Candidate;
import com.group1.recruitment.entity.Role;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.AccountStatus;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.repository.CandidateRepository;
import com.group1.recruitment.repository.RoleRepository;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CandidateRepository candidateRepository;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       CandidateRepository candidateRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.candidateRepository = candidateRepository;
    }

    /** Result of a sign-in attempt. */
    public record AuthResult(boolean success, boolean locked, User user) {
        static AuthResult failure() { return new AuthResult(false, false, null); }
        static AuthResult lockedOut() { return new AuthResult(false, true, null); }
        static AuthResult ok(User u) { return new AuthResult(true, false, u); }
    }

    /**
     * Authenticate by username OR email. Returns a generic failure for any wrong
     * credential (no account enumeration); signals a locked account separately.
     */
    public AuthResult authenticate(String usernameOrEmail, String rawPassword) {
        if (usernameOrEmail == null || rawPassword == null) {
            return AuthResult.failure();
        }
        String key = usernameOrEmail.trim();
        User user = userRepository.findByUsernameOrEmail(key, key).orElse(null);
        if (user == null) {
            return AuthResult.failure();
        }
        if (user.getStatus() == AccountStatus.LOCKED) {
            return AuthResult.lockedOut();
        }
        if (user.getStatus() != AccountStatus.ACTIVE) {
            return AuthResult.failure(); // INACTIVE cannot sign in
        }
        // NOTE: plaintext comparison to match the demo seeder. Replace with a
        // password encoder once Spring Security is added.
        if (!rawPassword.equals(user.getPasswordHash())) {
            return AuthResult.failure();
        }
        return AuthResult.ok(user);
    }

    @Transactional
    public User register(String fullName, String username, String email, String password, String confirm) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (isBlank(fullName)) errors.put("fullName", "Full name is required.");
        if (isBlank(username)) {
            errors.put("username", "Username is required.");
        } else if (!username.matches("[A-Za-z0-9_]{4,50}")) {
            errors.put("username", "4–50 characters. Letters, digits, and underscores only.");
        } else if (userRepository.existsByUsername(username)) {
            errors.put("username", "This username is already taken. Please choose another.");
        }
        if (isBlank(email)) {
            errors.put("email", "Email address is required.");
        } else if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            errors.put("email", "Please enter a valid email address.");
        } else if (userRepository.existsByEmail(email)) {
            errors.put("email", "This email address is already registered.");
        }
        String pwError = passwordComplexityError(password);
        if (pwError != null) errors.put("password", pwError);
        if (!nullSafeEquals(password, confirm)) errors.put("confirmPassword", "Passwords do not match.");

        if (!errors.isEmpty()) throw new ValidationException(errors);

        Role candidateRole = roleRepository.findByName(SessionUser.ROLE_CANDIDATE)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(SessionUser.ROLE_CANDIDATE);
                    r.setDescription("Candidate");
                    return roleRepository.save(r);
                });

        User user = new User();
        user.setFullName(fullName.trim());
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(password); // plaintext demo
        user.setRole(candidateRole);
        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        Candidate candidate = new Candidate();
        candidate.setUser(user);
        candidateRepository.save(candidate);

        return user;
    }

    @Transactional
    public void changePassword(Long userId, String current, String newPassword, String confirm) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ValidationException.global("User not found."));
        Map<String, String> errors = new LinkedHashMap<>();

        if (isBlank(current) || !current.equals(user.getPasswordHash())) {
            errors.put("currentPassword", "Incorrect current password.");
        }
        String pwError = passwordComplexityError(newPassword);
        if (pwError != null) {
            errors.put("newPassword", pwError);
        } else if (newPassword.equals(user.getPasswordHash())) {
            errors.put("newPassword", "New password must be different from your current password.");
        }
        if (!nullSafeEquals(newPassword, confirm)) {
            errors.put("confirmPassword", "Passwords do not match.");
        }
        if (!errors.isEmpty()) throw new ValidationException(errors);

        user.setPasswordHash(newPassword);
        userRepository.save(user);
    }

    /** @return an error message if the password fails complexity rules, else null. */
    private String passwordComplexityError(String password) {
        if (password == null || password.length() < 8) {
            return "At least 8 characters, including 1 uppercase letter and 1 number.";
        }
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasUpper || !hasDigit) {
            return "At least 8 characters, including 1 uppercase letter and 1 number.";
        }
        return null;
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private boolean nullSafeEquals(String a, String b) { return a != null && a.equals(b); }
}
