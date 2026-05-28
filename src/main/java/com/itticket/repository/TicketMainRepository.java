package com.itticket.repository;

import com.itticket.entity.TicketMain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketMainRepository extends JpaRepository<TicketMain, Long> {

    Optional<TicketMain> findByTicketNo(String ticketNo);

    Optional<TicketMain> findByIdAndDeleted(Long id, Integer deleted);

    Page<TicketMain> findByDeleted(Integer deleted, Pageable pageable);

    Page<TicketMain> findByReporterIdAndDeleted(Long reporterId, Integer deleted, Pageable pageable);

    Page<TicketMain> findByAssigneeIdAndDeleted(Long assigneeId, Integer deleted, Pageable pageable);

    Page<TicketMain> findByStatusAndDeleted(String status, Integer deleted, Pageable pageable);

    @Query("SELECT t FROM TicketMain t WHERE t.deleted = 0 AND " +
            "(:ticketNo IS NULL OR t.ticketNo LIKE %:ticketNo%) AND " +
            "(:title IS NULL OR t.title LIKE %:title%) AND " +
            "(:typeId IS NULL OR t.ticketType.id = :typeId) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:reporterId IS NULL OR t.reporter.id = :reporterId) AND " +
            "(:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND " +
            "(:priority IS NULL OR t.priority = :priority) AND " +
            "(:startTime IS NULL OR t.createdAt >= :startTime) AND " +
            "(:endTime IS NULL OR t.createdAt <= :endTime)")
    Page<TicketMain> searchTickets(
            @Param("ticketNo") String ticketNo,
            @Param("title") String title,
            @Param("typeId") Long typeId,
            @Param("status") String status,
            @Param("reporterId") Long reporterId,
            @Param("assigneeId") Long assigneeId,
            @Param("priority") Integer priority,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    @Query("SELECT t FROM TicketMain t WHERE t.deleted = 0 AND t.status = 'PENDING'")
    List<TicketMain> findPendingTickets();

    @Query("SELECT t FROM TicketMain t WHERE t.deleted = 0 AND t.status = 'SUSPENDED' AND t.slaPauseTime <= :expireTime")
    List<TicketMain> findExpiredSuspendedTickets(@Param("expireTime") LocalDateTime expireTime);

    @Query("SELECT t FROM TicketMain t WHERE t.deleted = 0 AND t.status IN ('PROCESSING', 'SUSPENDED') AND t.slaStartTime IS NOT NULL")
    List<TicketMain> findActiveTicketsForSlaCheck();

    @Query("SELECT t FROM TicketMain t WHERE t.deleted = 0 AND t.originalTicketId = :originalTicketId")
    List<TicketMain> findByOriginalTicketId(@Param("originalTicketId") Long originalTicketId);
}