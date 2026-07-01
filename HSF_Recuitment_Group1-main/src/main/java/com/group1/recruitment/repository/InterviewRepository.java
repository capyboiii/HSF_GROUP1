package com.group1.recruitment.repository;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.entity.Interview;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByApplicationOrderByInterviewDateDesc(Application application);

    List<Interview> findByInterviewerOrderByInterviewDateDesc(User interviewer);

    List<Interview> findByInterviewerAndStatus(User interviewer, InterviewStatus status);

    long countByInterviewDateBetween(LocalDate from, LocalDate to);
}
