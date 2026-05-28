package com.itticket.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetailResponse {

    private Long id;
    private String ticketNo;
    private Long typeId;
    private String typeName;
    private String title;
    private String description;
    private Long reporterId;
    private String reporterName;
    private Long assigneeId;
    private String assigneeName;
    private Integer urgency;
    private Integer impactScope;
    private Integer priority;
    private String status;
    private String statusDesc;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime slaStartTime;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
}