package com.internship.report.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EvaluationDTO {
    private Long id;
    private Long internshipId;
    private String studentName;
    private String studentNo;
    private String company;
    private String position;
    private String evaluatorName;
    private String evaluatorType;
    private Integer workPerformance;
    private Integer learningAbility;
    private Integer communication;
    private Integer punctuality;
    private Integer overall;
    private Double averageScore;
    private String strengths;
    private String weaknesses;
    private String suggestions;
    private LocalDateTime createdAt;
}
