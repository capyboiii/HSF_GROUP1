package com.group1.recruitment.controller;

import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.EventType;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import com.group1.recruitment.service.ActivityLogService;
import com.group1.recruitment.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * SCR-01 Login, SCR-02 Password Reset, SCR-03 Register, plus logout.
 */
@Controller
public class AuthController {

    private final AuthService authService;
    private final ActivityLogService activityLogService;

    public AuthController(AuthService authService, ActivityLogService activityLogService) {
        this.authService = authService;
        this.activityLogService = activityLogService;
    }

    // ---------------- SCR-01 Login ----------------

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        SessionUser user = SessionUtil.current(session);
        if (user != null) {
            return "redirect:" + user.homePath();
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String usernameOrEmail,
                        @RequestParam String password,
                        HttpServletRequest request,
                        HttpSession session,
                        Model model) {
        AuthService.AuthResult result = authService.authenticate(usernameOrEmail, password);
        if (result.success()) {
            User user = result.user();
            SessionUser sessionUser = new SessionUser(user);
            SessionUtil.login(session, sessionUser);
            activityLogService.log(user, EventType.SIGN_IN_SUCCESS, "Signed in", SessionUtil.clientIp(request));

            Object redirect = session.getAttribute("redirectAfterLogin");
            session.removeAttribute("redirectAfterLogin");
            if (redirect instanceof String target && !target.contains("/login")) {
                return "redirect:" + target;
            }
            return "redirect:" + sessionUser.homePath();
        }
        if (result.locked()) {
            model.addAttribute("lockout", true);
        } else {
            model.addAttribute("error", "Incorrect username or password.");
        }
        model.addAttribute("usernameOrEmail", usernameOrEmail);
        return "auth/login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ---------------- SCR-03 Register ----------------

    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        SessionUser user = SessionUtil.current(session);
        if (user != null) {
            return "redirect:" + user.homePath();
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                           @RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           RedirectAttributes ra,
                           Model model) {
        try {
            authService.register(fullName, username, email, password, confirmPassword);
            ra.addFlashAttribute("flash", "Account created successfully. Please sign in.");
            return "redirect:/login";
        } catch (ValidationException ex) {
            model.addAttribute("errors", ex.getErrors());
            model.addAttribute("fullName", fullName);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "auth/register";
        }
    }

    // ---------------- SCR-02 Password Reset ----------------

    @GetMapping("/reset-password")
    public String resetRequestPage() {
        return "auth/reset-request";
    }

    @PostMapping("/reset-password")
    public String resetRequest(@RequestParam String email, Model model) {
        // Always show the same message regardless of whether the email exists
        // (prevents account enumeration). Email delivery is out of scope for v1.
        model.addAttribute("submitted", true);
        return "auth/reset-request";
    }

    @GetMapping("/reset-password/confirm")
    public String resetConfirmPage(@RequestParam(required = false) String token, Model model) {
        // Demo build has no real token store; treat a missing token as an expired link.
        model.addAttribute("invalidToken", token == null || token.isBlank());
        return "auth/reset-confirm";
    }

    @PostMapping("/reset-password/confirm")
    public String resetConfirm(RedirectAttributes ra) {
        ra.addFlashAttribute("flash", "Password updated — please sign in with your new password.");
        return "redirect:/login";
    }
}
