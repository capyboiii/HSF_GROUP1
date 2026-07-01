package com.group1.recruitment.service;

import com.group1.recruitment.entity.*;
import com.group1.recruitment.enums.ApplicationStatus;
import com.group1.recruitment.enums.EventType;
import com.group1.recruitment.enums.JobStatus;
import com.group1.recruitment.exception.AccessDeniedException;
import com.group1.recruitment.exception.NotFoundException;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.repository.*;
import com.group1.recruitment.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final InterviewRepository interviewRepository;
    private final InternalNoteRepository internalNoteRepository;
    private final ActivityLogService activityLogService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              CandidateRepository candidateRepository,
                              InterviewRepository interviewRepository,
                              InternalNoteRepository internalNoteRepository,
                              ActivityLogService activityLogService) {
        this.applicationRepository = applicationRepository;
        this.candidateRepository = candidateRepository;
        this.interviewRepository = interviewRepository;
        this.internalNoteRepository = internalNoteRepository;
        this.activityLogService = activityLogService;
    }

    public Application getOrThrow(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found: " + id));
    }

    // ---------------- Candidate flows ----------------

    @Transactional
    public Application apply(User candidateUser, JobPosting job, String cvFileUrl) {
        if (job.getStatus() != JobStatus.ACTIVE) {
            throw ValidationException.global("This position is no longer accepting applications.");
        }
        Candidate candidate = candidateRepository.findByUser(candidateUser)
                .orElseThrow(() -> ValidationException.global("Only candidates can apply."));
        applicationRepository.findByCandidateAndJobPosting(candidate, job).ifPresent(a -> {
            throw ValidationException.global("You have already applied for this position.");
        });
        if (cvFileUrl == null || cvFileUrl.isBlank()) {
            throw ValidationException.global("A CV file is required to apply.");
        }
        Application app = new Application();
        app.setCandidate(candidate);
        app.setJobPosting(job);
        app.setStatus(ApplicationStatus.APPLIED);
        app.setSubmissionDate(LocalDateTime.now());
        app.setCvFileUrl(cvFileUrl);
        app = applicationRepository.save(app);
        activityLogService.log(candidateUser, EventType.APPLICATION_STATUS_CHANGED,
                "Applied to job: " + job.getTitle(), null);
        return app;
    }

    public boolean hasApplied(User candidateUser, JobPosting job) {
        return candidateRepository.findByUser(candidateUser)
                .flatMap(c -> applicationRepository.findByCandidateAndJobPosting(c, job))
                .isPresent();
    }

    public List<Application> myApplications(User candidateUser) {
        Candidate candidate = candidateRepository.findByUser(candidateUser)
                .orElseThrow(() -> new AccessDeniedException("Not a candidate account."));
        return applicationRepository.findByCandidateOrderBySubmissionDateDesc(candidate);
    }

    @Transactional
    public void withdraw(Long appId, User candidateUser) {
        Application app = getOrThrow(appId);
        Candidate candidate = candidateRepository.findByUser(candidateUser)
                .orElseThrow(() -> new AccessDeniedException("Not a candidate account."));
        if (!app.getCandidate().getId().equals(candidate.getId())) {
            throw new AccessDeniedException("This is not your application.");
        }
        if (app.getStatus() != ApplicationStatus.APPLIED && app.getStatus() != ApplicationStatus.SCREENING) {
            throw ValidationException.global("This application can no longer be withdrawn.");
        }
        app.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(app);
        activityLogService.log(candidateUser, EventType.APPLICATION_STATUS_CHANGED,
                "Withdrew application #" + app.getId(), null);
    }

    // ---------------- HR / Admin pipeline ----------------

    public List<Application> listForJob(JobPosting job, ApplicationStatus statusFilter) {
        if (statusFilter == null) {
            return applicationRepository.findByJobPostingOrderBySubmissionDateDesc(job);
        }
        return applicationRepository.findByJobPostingAndStatusOrderBySubmissionDateDesc(job, statusFilter);
    }

    /** Candidate counts per stage for pipeline charts (SCR-12 / SCR-20). */
    public Map<ApplicationStatus, Long> pipelineCounts(JobPosting job) {
        Map<ApplicationStatus, Long> counts = new LinkedHashMap<>();
        for (ApplicationStatus s : ApplicationStatus.values()) {
            counts.put(s, applicationRepository.countByJobPostingAndStatus(job, s));
        }
        return counts;
    }

    /** Whether a user may open the application detail (SCR-17). */
    public boolean canView(Application app, SessionUser user) {
        if (user.isAdmin()) return true;
        if (user.isHr()) {
            JobPosting job = app.getJobPosting();
            return job.getCreatedBy() != null && user.getId().equals(job.getCreatedBy().getId());
        }
        if (user.isInterviewer()) {
            return interviewRepository.findByApplicationOrderByInterviewDateDesc(app).stream()
                    .anyMatch(iv -> iv.getInterviewer() != null && user.getId().equals(iv.getInterviewer().getId()));
        }
        return false;
    }

    public void assertCanView(Application app, SessionUser user) {
        if (!canView(app, user)) {
            throw new AccessDeniedException("You do not have access to this application.");
        }
    }

    public void assertCanManage(Application app, SessionUser user) {
        boolean owner = user.isAdmin() || (user.isHr() && app.getJobPosting().getCreatedBy() != null
                && user.getId().equals(app.getJobPosting().getCreatedBy().getId()));
        if (!owner) {
            throw new AccessDeniedException("You cannot manage this application.");
        }
    }

    @Transactional
    public void advance(Long appId, SessionUser user, User actor) {
        Application app = getOrThrow(appId);
        assertCanManage(app, user);
        ApplicationStatus next = nextStage(app.getStatus());
        if (next == null) {
            throw ValidationException.global("This application cannot be advanced further.");
        }
        ApplicationStatus prev = app.getStatus();
        app.setStatus(next);
        applicationRepository.save(app);
        activityLogService.log(actor, EventType.APPLICATION_STATUS_CHANGED,
                "Application #" + app.getId() + " moved " + prev + " -> " + next, null);
    }

    @Transactional
    public void reject(Long appId, SessionUser user, User actor) {
        Application app = getOrThrow(appId);
        assertCanManage(app, user);
        if (isTerminal(app.getStatus())) {
            throw ValidationException.global("This application is already in a final state.");
        }
        app.setStatus(ApplicationStatus.REJECTED);
        applicationRepository.save(app);
        activityLogService.log(actor, EventType.APPLICATION_STATUS_CHANGED,
                "Application #" + app.getId() + " rejected", null);
    }

    @Transactional
    public void addNote(Long appId, SessionUser user, User author, String content) {
        Application app = getOrThrow(appId);
        assertCanManage(app, user);
        if (content == null || content.isBlank()) {
            throw ValidationException.global("Note content cannot be empty.");
        }
        InternalNote note = new InternalNote();
        note.setApplication(app);
        note.setAuthor(author);
        note.setContent(content.trim());
        note.setCreatedAt(LocalDateTime.now());
        internalNoteRepository.save(note);
    }

    public List<InternalNote> notes(Application app) {
        return internalNoteRepository.findByApplicationOrderByCreatedAtDesc(app);
    }

    /** Next pipeline stage, or null if terminal / no forward transition. */
    public ApplicationStatus nextStage(ApplicationStatus current) {
        return switch (current) {
            case APPLIED -> ApplicationStatus.SCREENING;
            case SCREENING -> ApplicationStatus.INTERVIEW;
            case INTERVIEW -> ApplicationStatus.OFFER;
            case OFFER -> ApplicationStatus.HIRED;
            default -> null;
        };
    }

    /** Label for the "advance" button given the current stage. */
    public String advanceLabel(ApplicationStatus current) {
        return switch (current) {
            case APPLIED -> "Move to Screening";
            case SCREENING -> "Move to Interview";
            case INTERVIEW -> "Move to Offer";
            case OFFER -> "Mark as Hired";
            default -> null;
        };
    }

    public boolean isTerminal(ApplicationStatus status) {
        return status == ApplicationStatus.HIRED
                || status == ApplicationStatus.REJECTED
                || status == ApplicationStatus.WITHDRAWN;
    }
}
