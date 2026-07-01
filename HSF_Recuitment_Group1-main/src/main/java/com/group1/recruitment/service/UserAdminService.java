package com.group1.recruitment.service;

import com.group1.recruitment.entity.Role;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.AccountStatus;
import com.group1.recruitment.enums.EventType;
import com.group1.recruitment.exception.NotFoundException;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.repository.RoleRepository;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ActivityLogService activityLogService;

    public UserAdminService(UserRepository userRepository, RoleRepository roleRepository,
                            ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.activityLogService = activityLogService;
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    /** Filtered user list for SCR-08. Role/status = null means "All". */
    public List<User> list(String roleName, AccountStatus status, String search) {
        String term = (search == null) ? null : search.trim().toLowerCase();
        return userRepository.findAll().stream()
                .filter(u -> roleName == null || (u.getRole() != null && roleName.equals(u.getRole().getName())))
                .filter(u -> status == null || u.getStatus() == status)
                .filter(u -> term == null || term.isEmpty()
                        || (u.getFullName() != null && u.getFullName().toLowerCase().contains(term))
                        || (u.getEmail() != null && u.getEmail().toLowerCase().contains(term)))
                .sorted((a, b) -> {
                    LocalDateTime ca = a.getCreatedAt(), cb = b.getCreatedAt();
                    if (ca == null && cb == null) return 0;
                    if (ca == null) return 1;
                    if (cb == null) return -1;
                    return cb.compareTo(ca);
                })
                .toList();
    }

    public Map<String, Long> roleCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("Admin", userRepository.countByRole_Name(SessionUser.ROLE_ADMIN));
        counts.put("HR Manager", userRepository.countByRole_Name(SessionUser.ROLE_HR));
        counts.put("Interviewer", userRepository.countByRole_Name(SessionUser.ROLE_INTERVIEWER));
        counts.put("Candidate", userRepository.countByRole_Name(SessionUser.ROLE_CANDIDATE));
        return counts;
    }

    public long lockedCount() {
        return userRepository.countByStatus(AccountStatus.LOCKED);
    }

    @Transactional
    public User createStaff(String fullName, String username, String email, String roleName,
                            String initialPassword, User actingAdmin) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (isBlank(fullName)) errors.put("fullName", "Full name is required.");
        if (isBlank(username)) {
            errors.put("username", "Username is required.");
        } else if (!username.matches("[A-Za-z0-9_]{4,50}")) {
            errors.put("username", "4–50 characters. Letters, digits, and underscores only.");
        } else if (userRepository.existsByUsername(username)) {
            errors.put("username", "This username is already taken.");
        }
        if (isBlank(email)) {
            errors.put("email", "Email is required.");
        } else if (userRepository.existsByEmail(email)) {
            errors.put("email", "This email address is already registered.");
        }
        if (!SessionUser.ROLE_HR.equals(roleName) && !SessionUser.ROLE_INTERVIEWER.equals(roleName)) {
            errors.put("role", "Role must be HR Manager or Interviewer.");
        }
        if (passwordWeak(initialPassword)) {
            errors.put("initialPassword", "At least 8 characters, including 1 uppercase letter and 1 number.");
        }
        if (!errors.isEmpty()) throw new ValidationException(errors);

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> ValidationException.of("role", "Role does not exist."));

        User user = new User();
        user.setFullName(fullName.trim());
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(initialPassword); // plaintext demo
        user.setRole(role);
        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        activityLogService.log(actingAdmin, EventType.ACCOUNT_CREATED,
                "Created account: " + user.getUsername() + " (" + roleName + ")", null);
        return user;
    }

    @Transactional
    public void deactivate(Long userId, User actingAdmin) {
        User user = getById(userId);
        // Prevent deactivating the last remaining active Admin.
        if (user.getRole() != null && SessionUser.ROLE_ADMIN.equals(user.getRole().getName())) {
            long activeAdmins = userRepository.findByRole_Name(SessionUser.ROLE_ADMIN).stream()
                    .filter(u -> u.getStatus() == AccountStatus.ACTIVE)
                    .count();
            if (activeAdmins <= 1) {
                throw ValidationException.global("Cannot deactivate the only remaining Admin account.");
            }
        }
        user.setStatus(AccountStatus.INACTIVE);
        userRepository.save(user);
        activityLogService.log(actingAdmin, EventType.ACCOUNT_DEACTIVATED,
                "Deactivated account: " + user.getUsername(), null);
    }

    @Transactional
    public void unlock(Long userId, User actingAdmin) {
        User user = getById(userId);
        user.setStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        activityLogService.log(actingAdmin, EventType.ACCOUNT_UNLOCKED,
                "Unlocked account: " + user.getUsername(), null);
    }

    private boolean passwordWeak(String p) {
        return p == null || p.length() < 8
                || p.chars().noneMatch(Character::isUpperCase)
                || p.chars().noneMatch(Character::isDigit);
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
