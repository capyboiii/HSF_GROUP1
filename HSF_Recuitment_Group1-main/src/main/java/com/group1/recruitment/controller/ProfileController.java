package com.group1.recruitment.controller;

import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import com.group1.recruitment.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** SCR-05 User Profile and SCR-04 Change Password. */
@Controller
public class ProfileController {

    private final AuthService authService;

    public ProfileController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes ra,
                                 Model model) {
        SessionUser user = SessionUtil.require(session);
        try {
            authService.changePassword(user.getId(), currentPassword, newPassword, confirmPassword);
            ra.addFlashAttribute("flash", "Password changed successfully.");
            return "redirect:/profile";
        } catch (ValidationException ex) {
            model.addAttribute("errors", ex.getErrors());
            return "change-password";
        }
    }
}
