package com.group1.recruitment.repository;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.entity.InternalNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InternalNoteRepository extends JpaRepository<InternalNote, Long> {
    List<InternalNote> findByApplicationOrderByCreatedAtDesc(Application application);
}
