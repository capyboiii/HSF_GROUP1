package com.group1.recruitment.controller;

import com.group1.recruitment.entity.JobPosting;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.JobStatus;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import com.group1.recruitment.service.ApplicationService;
import com.group1.recruitment.service.JobService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;

/** Candidate Portal: SCR-13 Public Job List, SCR-14 Public Job Detail + apply. */
@Controller
@RequestMapping("/jobs")
public class PublicJobController {

    private static final long MAX_CV_BYTES = 5 * 1024 * 1024;

    private final JobService jobService;
    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    public PublicJobController(JobService jobService, ApplicationService applicationService,
                               UserRepository userRepository) {
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.userRepository = userRepository;
    }

    // ---------------- SCR-13 Public Job List ----------------

    @GetMapping
    public String publicList(@RequestParam(required = false) String department,
                             @RequestParam(required = false) String location,
                             HttpSession session, Model model) {
        List<JobPosting> active = jobService.publicActiveJobs();
        List<String> departments = active.stream().map(JobPosting::getDepartment)
                .filter(d -> d != null && !d.isBlank()).distinct().sorted().toList();
        List<String> locations = active.stream().map(JobPosting::getLocation)
                .filter(l -> l != null && !l.isBlank()).distinct().sorted().toList();

        List<JobPosting> filtered = active.stream()
                .filter(j -> department == null || department.isBlank() || department.equals(j.getDepartment()))
                .filter(j -> location == null || location.isBlank() || location.equals(j.getLocation()))
                .toList();

        model.addAttribute("jobs", filtered);
        model.addAttribute("departments", departments);
        model.addAttribute("locations", locations);
        model.addAttribute("department", department);
        model.addAttribute("location", location);
        model.addAttribute("isGuest", SessionUtil.current(session) == null);
        return "public/job-list";
    }

    // ---------------- SCR-14 Public Job Detail ----------------

    @GetMapping("/{id}")
    public String publicDetail(@PathVariable Long id, HttpSession session, Model model) {
        JobPosting job = jobService.getOrThrow(id);
        SessionUser user = SessionUtil.current(session);

        boolean isGuest = user == null;
        boolean isCandidate = user != null && user.isCandidate();
        boolean isActive = job.getStatus() == JobStatus.ACTIVE;
        boolean alreadyApplied = false;
        if (isCandidate) {
            User candidateUser = userRepository.findById(user.getId()).orElse(null);
            alreadyApplied = candidateUser != null && applicationService.hasApplied(candidateUser, job);
        }

        model.addAttribute("job", job);
        model.addAttribute("isGuest", isGuest);
        model.addAttribute("isCandidate", isCandidate);
        model.addAttribute("isActive", isActive);
        model.addAttribute("alreadyApplied", alreadyApplied);
        model.addAttribute("canApply", isCandidate && isActive && !alreadyApplied);
        return "public/job-detail";
    }

    @PostMapping("/{id}/apply")
    public String apply(@PathVariable Long id,
                        @RequestParam(required = false) String coverLetter,
                        @RequestParam("cv") MultipartFile cv,
                        HttpSession session, RedirectAttributes ra) {
        SessionUser user = SessionUtil.require(session);
        JobPosting job = jobService.getOrThrow(id);
        if (!user.isCandidate()) {
            ra.addFlashAttribute("error", "Only candidates can apply for positions.");
            return "redirect:/jobs/" + id;
        }
        User candidateUser = userRepository.findById(user.getId()).orElse(null);
        try {
            String cvUrl = validateAndStoreCv(cv);
            applicationService.apply(candidateUser, job, cvUrl);
            ra.addFlashAttribute("applied", true);
            ra.addFlashAttribute("flash", "Your application has been submitted successfully.");
        } catch (ValidationException ex) {
            ra.addFlashAttribute("error", ex.getErrors().getOrDefault("global", "Unable to submit application."));
        }
        return "redirect:/jobs/" + id;
    }

    /**
     * Validates the uploaded CV (PDF/DOCX, max 5 MB) and returns a stored path.
     * NOTE: demo build does not persist bytes; it records a reference path only.
     */
    private String validateAndStoreCv(MultipartFile cv) {
        if (cv == null || cv.isEmpty()) {
            throw ValidationException.global("A CV file is required (PDF or DOCX, max 5 MB).");
        }
        if (cv.getSize() > MAX_CV_BYTES) {
            throw ValidationException.global("CV file must be 5 MB or smaller.");
        }
        String name = cv.getOriginalFilename() == null ? "cv" : cv.getOriginalFilename();
        String lower = name.toLowerCase(Locale.ROOT);
        if (!lower.endsWith(".pdf") && !lower.endsWith(".docx")) {
            throw ValidationException.global("CV must be a PDF or DOCX file.");
        }
        return "/cv/" + name.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
