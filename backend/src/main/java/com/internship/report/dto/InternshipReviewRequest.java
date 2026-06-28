package com.internship.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InternshipReviewRequest {
    @NotBlank(message = "审核意见不能为空")
    private String comment;
    
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;
}
