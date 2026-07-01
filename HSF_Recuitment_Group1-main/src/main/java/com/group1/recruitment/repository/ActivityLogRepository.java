package com.group1.recruitment.repository;

import com.group1.recruitment.entity.ActivityLog;
import com.group1.recruitment.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findTop10ByOrderByTimestampDesc();

    @Query("select a from ActivityLog a where "
            + "(:eventType is null or a.eventType = :eventType) and "
            + "(:actor is null or lower(a.user.username) like lower(concat('%', :actor, '%')) "
            + "     or lower(a.user.fullName) like lower(concat('%', :actor, '%'))) and "
            + "(:from is null or a.timestamp >= :from) and "
            + "(:to is null or a.timestamp <= :to) "
            + "order by a.timestamp desc")
    List<ActivityLog> search(@Param("eventType") EventType eventType,
                             @Param("actor") String actor,
                             @Param("from") LocalDateTime from,
                             @Param("to") LocalDateTime to);
}
