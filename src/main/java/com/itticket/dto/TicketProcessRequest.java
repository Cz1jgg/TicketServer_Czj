package com.itticket.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketProcessRequest {

    @NotNull(message = "工单ID不能为空")
    private Long ticketId;
    
    private String remark;
}