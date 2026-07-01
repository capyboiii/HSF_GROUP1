package com.group1.recruitment.controller;

import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.AccountStatus;
import com.group1.recruitment.enums.EventType;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import com.group1.recruitment.service.ActivityLogService;
import com.group1.recruitment.service.UserAdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** SCR-08 User Management and SCR-09 Activity Log. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserAdminService userAdminService;
    private final ActivityLogService activityLogService;
    private final UserRepository userRepository;

    public AdminController(UserAdminService userAdminService, ActivityLogService activityLogService,
                           UserRepository userRepository) {
        this.userAdminService = userAdminService;
        this.activityLogService = activityLogService;
        this.userRepository = userRepository;
    }

    // ---------------- SCR-08 User Management ----------------

    @GetMapping("/users")
    public String users(@RequestParam(required = false) String role,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String q,
                        Model model) {
        AccountStatus statusFilter = parseStatus(status);
        model.addAttribute("users", userAdminService.list(emptyToNull(role), statusFilter, q));
        model.addAttribute("roleFilter", role);
        model.addAttribute("statusFilter", status);
        model.addAttribute("q", q);
        return "admin/users";
    }

    @PostMapping("/users")
    public String createUser(@RequestParam String fullName,
                             @RequestParam String username,
                             @RequestParam String email,
                             @RequestParam String role,
                             @RequestParam String initialPassword,
                             HttpSession session,
                             RedirectAttributes ra) {
        SessionUser sessionUser = SessionUtil.require(session);
        User admin = userRepository.findById(sessionUser.getId()).orElse(null);
        try {
            User created = userAdminService.createStaff(fullName, username, email, role, initialPassword, admin);
            ra.addFlashAttribute("flash", "Account created: " + created.getUsername());
        } catch (ValidationException ex) {
            ra.addFlashAttribute("createErrors", ex.getErrors());
            ra.addFlashAttribute("openCreate", true);
            ra.addFlashAttribute("fullName", fullName);
            ra.addFlashAttribute("username", username);
            ra.addFlashAttribute("email", email);
            ra.addFlashAttribute("role", role);
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/deactivate")
    public String deactivate(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        SessionUser sessionUser = SessionUtil.require(session);
        User admin = userRepository.findById(sessionUser.getId()).orElse(null);
        try {
            userAdminService.deactivate(id, admin);
            ra.addFlashAttribute("flash", "Account deactivated.");
        } catch (ValidationException ex) {
            ra.addFlashAttribute("error", ex.getErrors().getOrDefault("global", "Unable to deactivate account."));
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/unlock")
    public String unlock(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        SessionUser sessionUser = SessionUtil.require(session);
        User admin = userRepository.findById(sessionUser.getId()).orElse(null);
        userAdminService.unlock(id, admin);
        ra.addFlashAttribute("flash", "Account unlocked.");
        return "redirect:/admin/users";
    }

    // ---------------- SCR-09 Activity Log ----------------

    @GetMapping("/activity-log")
    public String activityLog(@RequestParam(required = false) String eventType,
                              @RequestParam(required = false) String actor,
                              @RequestParam(required = false) String from,
                              @RequestParam(required = false) String to,
                              Model model) {
        EventType type = parseEventType(eventType);
        LocalDateTime fromDt = parseDate(from, false);
        LocalDateTime toDt = parseDate(to, true);
        model.addAttribute("logs", activityLogService.search(type, actor, fromDt, toDt));
        model.addAttribute("eventTypes", EventType.values());
        model.addAttribute("eventType", eventType);
        model.addAttribute("actor", actor);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "admin/activity-log";
    }

    private AccountStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return AccountStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private EventType parseEventType(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return EventType.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private LocalDateTime parseDate(String value, boolean endOfDay) {
        if (value == null || value.isBlank()) return null;
        try {
            LocalDate d = LocalDate.parse(value);
            return endOfDay ? d.atTime(23, 59, 59) : d.atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
