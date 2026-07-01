package com.group1.recruitment.controller;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.entity.Interview;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.InterviewStatus;
import com.group1.recruitment.exception.AccessDeniedException;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import com.group1.recruitment.service.ApplicationService;
import com.group1.recruitment.service.InterviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/** SCR-17 Application Detail, SCR-18 Interview Assignment, SCR-19 Evaluation, interviewer landing. */
@Controller
public class ApplicationController {

    private final ApplicationService applicationService;
    private final InterviewService interviewService;
    private final UserRepository userRepository;

    public ApplicationController(ApplicationService applicationService, InterviewService interviewService,
                                 UserRepository userRepository) {
        this.applicationService = applicationService;
        this.interviewService = interviewService;
        this.userRepository = userRepository;
    }

    private User entity(SessionUser user) {
        return userRepository.findById(user.getId()).orElseThrow();
    }

    // ---------------- Interviewer landing ----------------

    @GetMapping("/interviewer")
    public String interviewerHome(HttpSession session, Model model) {
        SessionUser user = SessionUtil.require(session);
        List<Interview> interviews = interviewService.forInterviewer(entity(user));
        model.addAttribute("interviews", interviews);
        return "interviewer/assignments";
    }

    // ---------------- SCR-17 Application Detail ----------------

    @GetMapping("/applications/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        SessionUser user = SessionUtil.require(session);
        Application app = applicationService.getOrThrow(id);
        applicationService.assertCanView(app, user);

        boolean isManager = user.isAdmin()
                || (user.isHr() && app.getJobPosting().getCreatedBy() != null
                    && user.getId().equals(app.getJobPosting().getCreatedBy().getId()));

        model.addAttribute("app", app);
        model.addAttribute("isManager", isManager);
        model.addAttribute("isInterviewer", user.isInterviewer());
        model.addAttribute("terminal", applicationService.isTerminal(app.getStatus()));

        List<Interview> interviews = interviewService.forApplication(app);
        model.addAttribute("interviews", interviews);

        if (isManager) {
            model.addAttribute("notes", applicationService.notes(app));
            model.addAttribute("advanceLabel", applicationService.advanceLabel(app.getStatus()));
        }
        if (user.isInterviewer()) {
            Interview mine = interviews.stream()
                    .filter(iv -> iv.getInterviewer() != null && user.getId().equals(iv.getInterviewer().getId()))
                    .findFirst().orElse(null);
            model.addAttribute("myInterview", mine);
        }
        return "application/detail";
    }

    @PostMapping("/applications/{id}/advance")
    public String advance(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        SessionUser user = SessionUtil.require(session);
        try {
            applicationService.advance(id, user, entity(user));
            ra.addFlashAttribute("flash", "Candidate advanced to the next stage.");
        } catch (ValidationException ex) {
            ra.addFlashAttribute("error", ex.getErrors().getOrDefault("global", "Unable to advance."));
        }
        return "redirect:/applications/" + id;
    }

    @PostMapping("/applications/{id}/reject")
    public String reject(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        SessionUser user = SessionUtil.require(session);
        try {
            applicationService.reject(id, user, entity(user));
            ra.addFlashAttribute("flash", "Candidate rejected.");
        } catch (ValidationException ex) {
            ra.addFlashAttribute("error", ex.getErrors().getOrDefault("global", "Unable to reject."));
        }
        return "redirect:/applications/" + id;
    }

    @PostMapping("/applications/{id}/note")
    public String addNote(@PathVariable Long id, @RequestParam String content,
                          HttpSession session, RedirectAttributes ra) {
        SessionUser user = SessionUtil.require(session);
        try {
            applicationService.addNote(id, user, entity(user), content);
            ra.addFlashAttribute("flash", "Note added.");
        } catch (ValidationException ex) {
            ra.addFlashAttribute("error", ex.getErrors().getOrDefault("global", "Unable to add note."));
        }
        return "redirect:/applications/" + id;
    }

    // ---------------- SCR-18 Interview Assignment ----------------

    @GetMapping("/applications/{id}/assign-interview")
    public String assignForm(@PathVariable Long id, HttpSession session, Model model) {
        SessionUser user = SessionUtil.require(session);
        Application app = applicationService.getOrThrow(id);
        applicationService.assertCanManage(app, user);
        model.addAttribute("app", app);
        model.addAttribute("interviewers", interviewService.activeInterviewers());
        model.addAttribute("today", LocalDate.now());
        return "interview/assign";
    }

    @PostMapping("/applications/{id}/assign-interview")
    public String assign(@PathVariable Long id,
                         @RequestParam Long interviewerId,
                         @RequestParam String interviewDate,
                         @RequestParam String interviewTime,
                         @RequestParam(required = false) String locationOrLink,
                         HttpSession session, RedirectAttributes ra, Model model) {
        SessionUser user = SessionUtil.require(session);
        Application app = applicationService.getOrThrow(id);
        applicationService.assertCanManage(app, user);
        try {
            LocalDate date = interviewDate == null || interviewDate.isBlank() ? null : LocalDate.parse(interviewDate);
            Interview iv = interviewService.assign(app, interviewerId, date, interviewTime, locationOrLink, entity(user));
            ra.addFlashAttribute("flash", "Interview scheduled. " + iv.getInterviewer().getFullName() + " has been assigned.");
            return "redirect:/applications/" + id;
        } catch (ValidationException ex) {
            model.addAttribute("errors", ex.getErrors());
            model.addAttribute("app", app);
            model.addAttribute("interviewers", interviewService.activeInterviewers());
            model.addAttribute("today", LocalDate.now());
            return "interview/assign";
        }
    }

    // ---------------- SCR-19 Evaluation Form ----------------

    @GetMapping("/applications/{id}/evaluate")
    public String evaluateForm(@PathVariable Long id, @RequestParam Long interviewId,
                               HttpSession session, Model model) {
        SessionUser user = SessionUtil.require(session);
        Application app = applicationService.getOrThrow(id);
        Interview interview = interviewService.getOrThrow(interviewId);
        if (interview.getInterviewer() == null || !user.getId().equals(interview.getInterviewer().getId())) {
            throw new AccessDeniedException("You are not assigned to this interview.");
        }
        model.addAttribute("app", app);
        model.addAttribute("interview", interview);
        model.addAttribute("alreadyEvaluated", interview.getStatus() == InterviewStatus.EVALUATED);
        model.addAttribute("evaluation", interviewService.evaluationOf(interview));
        return "evaluation/form";
    }

    @PostMapping("/applications/{id}/evaluate")
    public String submitEvaluation(@PathVariable Long id,
                                   @RequestParam Long interviewId,
                                   @RequestParam(required = false) Integer rating,
                                   @RequestParam(required = false) String feedback,
                                   HttpSession session, RedirectAttributes ra, Model model) {
        SessionUser user = SessionUtil.require(session);
        try {
            interviewService.submitEvaluation(interviewId, user, entity(user), rating, feedback);
            ra.addFlashAttribute("flash", "Evaluation submitted. Thank you.");
            return "redirect:/applications/" + id;
        } catch (ValidationException ex) {
            Application app = applicationService.getOrThrow(id);
            Interview interview = interviewService.getOrThrow(interviewId);
            model.addAttribute("errors", ex.getErrors());
            model.addAttribute("app", app);
            model.addAttribute("interview", interview);
            model.addAttribute("alreadyEvaluated", interview.getStatus() == InterviewStatus.EVALUATED);
            model.addAttribute("rating", rating);
            model.addAttribute("feedback", feedback);
            return "evaluation/form";
        }
    }
}
