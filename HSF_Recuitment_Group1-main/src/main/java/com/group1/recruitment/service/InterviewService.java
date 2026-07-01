package com.group1.recruitment.service;

import com.group1.recruitment.entity.*;
import com.group1.recruitment.enums.AccountStatus;
import com.group1.recruitment.enums.ApplicationStatus;
import com.group1.recruitment.enums.EventType;
import com.group1.recruitment.enums.InterviewStatus;
import com.group1.recruitment.exception.AccessDeniedException;
import com.group1.recruitment.exception.NotFoundException;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.repository.EvaluationRepository;
import com.group1.recruitment.repository.InterviewRepository;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final EvaluationRepository evaluationRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public InterviewService(InterviewRepository interviewRepository, EvaluationRepository evaluationRepository,
                            UserRepository userRepository, ActivityLogService activityLogService) {
        this.interviewRepository = interviewRepository;
        this.evaluationRepository = evaluationRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
    }

    public Interview getOrThrow(Long id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Interview not found: " + id));
    }

    public List<User> activeInterviewers() {
        return userRepository.findByRole_Name(SessionUser.ROLE_INTERVIEWER).stream()
                .filter(u -> u.getStatus() == AccountStatus.ACTIVE)
                .sorted((a, b) -> String.valueOf(a.getFullName()).compareToIgnoreCase(String.valueOf(b.getFullName())))
                .toList();
    }

    public List<Interview> forApplication(Application app) {
        return interviewRepository.findByApplicationOrderByInterviewDateDesc(app);
    }

    public List<Interview> forInterviewer(User interviewer) {
        return interviewRepository.findByInterviewerOrderByInterviewDateDesc(interviewer);
    }

    @Transactional
    public Interview assign(Application app, Long interviewerId, LocalDate date, String timeText,
                            String locationOrLink, User actor) {
        if (app.getStatus() != ApplicationStatus.INTERVIEW) {
            throw ValidationException.global("Interviews can only be scheduled while the application is at the Interview stage.");
        }
        User interviewer = userRepository.findById(interviewerId)
                .filter(u -> u.getRole() != null && SessionUser.ROLE_INTERVIEWER.equals(u.getRole().getName()))
                .filter(u -> u.getStatus() == AccountStatus.ACTIVE)
                .orElseThrow(() -> ValidationException.of("interviewerId", "Please select a valid Interviewer."));

        if (date == null) {
            throw ValidationException.of("interviewDate", "Interview date is required.");
        }
        LocalTime time = parseTime(timeText);
        LocalDateTime scheduledAt = LocalDateTime.of(date, time);
        if (scheduledAt.isBefore(LocalDateTime.now())) {
            throw ValidationException.of("interviewDate", "Interview must be scheduled for a future date and time.");
        }
        if (locationOrLink != null && locationOrLink.length() > 500) {
            throw ValidationException.of("locationOrLink", "Max 500 characters.");
        }

        Interview interview = new Interview();
        interview.setApplication(app);
        interview.setInterviewer(interviewer);
        interview.setInterviewDate(date);
        interview.setInterviewTime(time);
        interview.setLocationOrLink(locationOrLink);
        interview.setStatus(InterviewStatus.SCHEDULED);
        interview = interviewRepository.save(interview);

        activityLogService.log(actor, EventType.APPLICATION_STATUS_CHANGED,
                "Interview scheduled for application #" + app.getId() + " with " + interviewer.getFullName(), null);
        return interview;
    }

    @Transactional
    public void submitEvaluation(Long interviewId, SessionUser user, User actor, Integer rating, String feedback) {
        Interview interview = getOrThrow(interviewId);
        if (interview.getInterviewer() == null || !user.getId().equals(interview.getInterviewer().getId())) {
            throw new AccessDeniedException("You are not assigned to this interview.");
        }
        if (interview.getStatus() == InterviewStatus.EVALUATED) {
            throw ValidationException.global("This evaluation has already been submitted and cannot be changed.");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw ValidationException.of("rating", "Please select a rating from 1 to 5 stars.");
        }
        if (feedback == null || feedback.isBlank()) {
            throw ValidationException.of("feedback", "Written feedback is required.");
        }
        Evaluation evaluation = new Evaluation();
        evaluation.setInterview(interview);
        evaluation.setRating(rating);
        evaluation.setFeedback(feedback.trim());
        evaluation.setSubmittedAt(LocalDateTime.now());
        evaluationRepository.save(evaluation);

        interview.setStatus(InterviewStatus.EVALUATED);
        interview.setEvaluation(evaluation);
        interviewRepository.save(interview);

        activityLogService.log(actor, EventType.EVALUATION_SUBMITTED,
                "Evaluation submitted for application #" + interview.getApplication().getId(), null);
    }

    public Evaluation evaluationOf(Interview interview) {
        return evaluationRepository.findByInterview(interview).orElse(null);
    }

    private LocalTime parseTime(String timeText) {
        if (timeText == null || timeText.isBlank()) {
            throw ValidationException.of("interviewTime", "Interview time is required (HH:mm).");
        }
        try {
            return LocalTime.parse(timeText.trim(), DateTimeFormatter.ofPattern("H:mm"));
        } catch (Exception e) {
            try {
                return LocalTime.parse(timeText.trim());
            } catch (Exception ex) {
                throw ValidationException.of("interviewTime", "Time must be in HH:mm format (24-hour).");
            }
        }
    }
}
