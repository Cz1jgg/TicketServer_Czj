package com.itticket.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketAssignRequest {

    @NotNull(message = "工单ID不能为空")
    private Long ticketId;

    @NotNull(message = "分配用户ID不能为空")
    private Long assigneeId;

    private String remark;
}