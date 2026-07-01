package com.group1.recruitment.controller;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.ApplicationStatus;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import com.group1.recruitment.service.ApplicationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** SCR-15 My Applications (Candidate). */
@Controller
public class MyApplicationController {

    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    public MyApplicationController(ApplicationService applicationService, UserRepository userRepository) {
        this.applicationService = applicationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/my-applications")
    public String myApplications(@RequestParam(required = false) String status,
                                 HttpSession session, Model model) {
        SessionUser user = SessionUtil.require(session);
        User candidateUser = userRepository.findById(user.getId()).orElseThrow();
        List<Application> apps = applicationService.myApplications(candidateUser);

        ApplicationStatus filter = parse(status);
        List<Application> filtered = filter == null ? apps
                : apps.stream().filter(a -> a.getStatus() == filter).toList();

        model.addAttribute("applications", filtered);
        model.addAttribute("stages", ApplicationStatus.values());
        model.addAttribute("activeStage", filter == null ? "ALL" : filter.name());
        model.addAttribute("hasAny", !apps.isEmpty());
        return "candidate/my-applications";
    }

    @PostMapping("/my-applications/{id}/withdraw")
    public String withdraw(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        SessionUser user = SessionUtil.require(session);
        User candidateUser = userRepository.findById(user.getId()).orElseThrow();
        try {
            applicationService.withdraw(id, candidateUser);
            ra.addFlashAttribute("flash", "Application withdrawn.");
        } catch (ValidationException ex) {
            ra.addFlashAttribute("error", ex.getErrors().getOrDefault("global", "Unable to withdraw."));
        }
        return "redirect:/my-applications";
    }

    private ApplicationStatus parse(String status) {
        if (status == null || status.isBlank() || status.equalsIgnoreCase("ALL")) return null;
        try {
            return ApplicationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
