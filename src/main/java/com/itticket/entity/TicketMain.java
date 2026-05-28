package com.itticket.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ticket_main")
public class TicketMain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_no", nullable = false, unique = true, length = 50)
    private String ticketNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private TicketType ticketType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private SysUser reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private SysUser assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sla_rule_id", nullable = false)
    private SlaRule slaRule;

    @Column(name = "urgency", nullable = false)
    private Integer urgency;

    @Column(name = "impact_scope", nullable = false)
    private Integer impactScope;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "sla_start_time")
    private LocalDateTime slaStartTime;

    @Column(name = "sla_pause_time")
    private LocalDateTime slaPauseTime;

    @Column(name = "sla_pause_duration")
    private Long slaPauseDuration;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "deleted", nullable = false)
    private Integer deleted;

    @Column(name = "original_ticket_id")
    private Long originalTicketId;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        deleted = 0;
        status = "PENDING";
        urgency = 1;
        impactScope = 1;
        priority = 1;
        slaPauseDuration = 0L;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}