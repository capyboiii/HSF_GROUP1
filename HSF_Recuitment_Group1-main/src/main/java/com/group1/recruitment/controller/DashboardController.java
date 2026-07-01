package com.group1.recruitment.controller;

import com.group1.recruitment.entity.User;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import com.group1.recruitment.service.ActivityLogService;
import com.group1.recruitment.service.ReportService;
import com.group1.recruitment.service.UserAdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** SCR-06 HR Dashboard and SCR-07 Admin Dashboard. */
@Controller
public class DashboardController {

    private final ReportService reportService;
    private final UserAdminService userAdminService;
    private final ActivityLogService activityLogService;
    private final UserRepository userRepository;

    public DashboardController(ReportService reportService, UserAdminService userAdminService,
                              ActivityLogService activityLogService, UserRepository userRepository) {
        this.reportService = reportService;
        this.userAdminService = userAdminService;
        this.activityLogService = activityLogService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String hrDashboard(HttpSession session, Model model) {
        SessionUser user = SessionUtil.require(session);
        User managed = userRepository.findById(user.getId()).orElse(null);
        model.addAttribute("stats", reportService.stats(user, managed));
        model.addAttribute("activeJobs", reportService.activeJobs(user, managed));
        return "dashboard/hr";
    }

    @GetMapping("/admin")
    public String adminDashboard(HttpSession session, Model model) {
        SessionUser user = SessionUtil.require(session);
        User managed = userRepository.findById(user.getId()).orElse(null);
        model.addAttribute("stats", reportService.stats(user, managed));
        model.addAttribute("activeJobs", reportService.activeJobs(user, managed));
        var roleCounts = userAdminService.roleCounts();
        model.addAttribute("roleCounts", roleCounts);
        model.addAttribute("totalUsers", roleCounts.values().stream().mapToLong(Long::longValue).sum());
        model.addAttribute("lockedCount", userAdminService.lockedCount());
        model.addAttribute("recentActivity", activityLogService.recent());
        return "dashboard/admin";
    }
}
