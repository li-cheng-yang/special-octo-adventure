package com.internship.report.dto;

import lombok.Data;

@Data
public class ReportQueryRequest {
    private Long studentId;
    private Long internshipId;
    private String status;
    private String reportType;
    private String title;
    private Integer page = 0;
    private Integer size = 10;
}
