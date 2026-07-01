package com.group1.recruitment.controller;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.entity.JobPosting;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.exception.AccessDeniedException;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import com.group1.recruitment.service.ApplicationService;
import com.group1.recruitment.service.JobService;
import com.group1.recruitment.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** SCR-20 Pipeline Report. */
@Controller
public class ReportController {

    private final ReportService reportService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    public ReportController(ReportService reportService, JobService jobService,
                            ApplicationService applicationService, UserRepository userRepository) {
        this.reportService = reportService;
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/reports")
    public String report(@RequestParam(name = "jobId", required = false) Long jobId,
                         HttpSession session, Model model) {
        SessionUser user = SessionUtil.require(session);
        User managed = userRepository.findById(user.getId()).orElse(null);
        List<JobPosting> jobs = reportService.reportableJobs(user, managed);

        JobPosting selected = null;
        if (jobId != null) {
            selected = jobService.getOrThrow(jobId);
            if (!jobService.canManage(selected, user)) {
                throw new AccessDeniedException("You cannot view this job's report.");
            }
        } else if (!jobs.isEmpty()) {
            selected = jobs.get(0); // default to most recently updated
        }

        model.addAttribute("jobs", jobs);
        model.addAttribute("selected", selected);
        if (selected != null) {
            model.addAttribute("pipeline", applicationService.pipelineCounts(selected));
            List<Application> apps = applicationService.listForJob(selected, null);
            model.addAttribute("applications", apps);
            model.addAttribute("applicationCount", apps.size());
        }
        return "report/pipeline";
    }
}
