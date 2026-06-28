package com.internship.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportReviewRequest {
    @NotBlank(message = "批阅意见不能为空")
    private String comment;
    
    private Integer score;
    
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;
}
