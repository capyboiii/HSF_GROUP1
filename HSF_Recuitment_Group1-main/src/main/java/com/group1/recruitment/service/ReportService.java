package com.group1.recruitment.service;

import com.group1.recruitment.entity.JobPosting;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.ApplicationStatus;
import com.group1.recruitment.enums.JobStatus;
import com.group1.recruitment.repository.ApplicationRepository;
import com.group1.recruitment.repository.InterviewRepository;
import com.group1.recruitment.repository.JobPostingRepository;
import com.group1.recruitment.security.SessionUser;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/** Aggregations for the HR dashboard (SCR-06), Admin dashboard (SCR-07) and pipeline report (SCR-20). */
@Service
public class ReportService {

    private final JobPostingRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;

    public ReportService(JobPostingRepository jobRepository, ApplicationRepository applicationRepository,
                         InterviewRepository interviewRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
    }

    /** Snapshot counts for a dashboard. */
    public record DashboardStats(long activeJobs, long applicationsAwaitingReview, long upcomingInterviews) {
    }

    /**
     * HR sees stats scoped to their own postings; Admin sees everything.
     * @param managed the managed User entity for the HR Manager (ignored for Admin)
     */
    public DashboardStats stats(SessionUser user, User managed) {
        long upcoming = interviewRepository.countByInterviewDateBetween(LocalDate.now(), LocalDate.now().plusDays(7));
        if (user.isAdmin()) {
            return new DashboardStats(
                    jobRepository.countByStatus(JobStatus.ACTIVE),
                    applicationRepository.countByStatus(ApplicationStatus.APPLIED),
                    upcoming);
        }
        return new DashboardStats(
                jobRepository.countByCreatedByAndStatus(managed, JobStatus.ACTIVE),
                applicationRepository.countByJobPosting_CreatedByAndStatus(managed, ApplicationStatus.APPLIED),
                upcoming);
    }

    /** Active jobs table for the dashboard, scoped by role. */
    public List<JobPosting> activeJobs(SessionUser user, User managed) {
        if (user.isAdmin()) {
            return jobRepository.findByStatusOrderByCreatedAtDesc(JobStatus.ACTIVE);
        }
        return jobRepository.findByCreatedByOrderByCreatedAtDesc(managed).stream()
                .filter(j -> j.getStatus() == JobStatus.ACTIVE)
                .toList();
    }

    /** Jobs available in the SCR-20 report selector, scoped by role. */
    public List<JobPosting> reportableJobs(SessionUser user, User managed) {
        if (user.isAdmin()) {
            return jobRepository.findAllByOrderByCreatedAtDesc();
        }
        return jobRepository.findByCreatedByOrderByCreatedAtDesc(managed);
    }

    public long applicationCount(JobPosting job) {
        return applicationRepository.countByJobPosting(job);
    }
}
