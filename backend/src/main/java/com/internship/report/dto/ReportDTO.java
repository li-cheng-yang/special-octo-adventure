package com.internship.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
    private Long id;
    private Long internshipId;
    private String company;
    private Long studentId;
    private String studentName;
    private String studentNo;
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
    private Long reviewerId;
    private String reviewerName;
    private String reviewComment;
    private Integer score;
    private String reviewTime;
    private String createTime;
    private String updateTime;
}
