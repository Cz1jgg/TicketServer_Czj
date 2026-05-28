package com.itticket.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketQueryRequest {

    private String ticketNo;
    private String title;
    private Long typeId;
    private String status;
    private Long reporterId;
    private Long assigneeId;
    private Integer priority;
    private String startTime;
    private String endTime;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}