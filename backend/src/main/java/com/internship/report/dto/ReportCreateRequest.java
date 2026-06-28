package com.internship.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReportCreateRequest {
    @NotNull(message = "实习ID不能为空")
    private Long internshipId;
    
    @NotBlank(message = "标题不能为空")
    private String title;
    
    @NotBlank(message = "汇报类型不能为空")
    private String reportType;
    
    private String content;
    
    private String summary;
    
    private String problem;
    
    private String solution;
    
    private String plan;
    
    private LocalDate reportDate;
    
    private Integer weekNumber;
}
