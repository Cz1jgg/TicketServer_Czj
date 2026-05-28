package com.itticket.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCreateRequest {

    @NotNull(message = "工单类型ID不能为空")
    private Long typeId;

    @NotBlank(message = "工单标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "紧急程度不能为空")
    @Min(value = 1, message = "紧急程度最小为1")
    @Max(value = 3, message = "紧急程度最大为3")
    private Integer urgency;

    @NotNull(message = "影响范围不能为空")
    @Min(value = 1, message = "影响范围最小为1")
    @Max(value = 3, message = "影响范围最大为3")
    private Integer impactScope;
}