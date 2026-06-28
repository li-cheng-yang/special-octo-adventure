package com.internship.report.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReportUpdateRequest {
    private String title;
    private String reportType;
    private String content;
    private String summary;
    private String problem;
    private String solution;
    private String plan;
    private LocalDate reportDate;
    private Integer weekNumber;
    private String status;
}
