package com.group1.recruitment.service;

import com.group1.recruitment.entity.ActivityLog;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.EventType;
import com.group1.recruitment.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public void log(User actor, EventType type, String description, String ip) {
        ActivityLog log = new ActivityLog();
        log.setUser(actor);
        log.setEventType(type);
        log.setDescription(description);
        log.setIpAddress(ip);
        log.setTimestamp(LocalDateTime.now());
        activityLogRepository.save(log);
    }

    public List<ActivityLog> recent() {
        return activityLogRepository.findTop10ByOrderByTimestampDesc();
    }

    public List<ActivityLog> search(EventType type, String actor, LocalDateTime from, LocalDateTime to) {
        String actorTerm = (actor == null || actor.trim().isEmpty()) ? null : actor.trim();
        return activityLogRepository.search(type, actorTerm, from, to);
    }
}
