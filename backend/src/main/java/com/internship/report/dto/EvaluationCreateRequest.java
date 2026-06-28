package com.internship.report.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EvaluationCreateRequest {

    @NotNull
    private Long internshipId;

    @NotNull
    private String evaluatorType;  // TEACHER or COMPANY

    @NotNull @Min(1) @Max(100)
    private Integer workPerformance;

    @NotNull @Min(1) @Max(100)
    private Integer learningAbility;

    @NotNull @Min(1) @Max(100)
    private Integer communication;

    @NotNull @Min(1) @Max(100)
    private Integer punctuality;

    @NotNull @Min(1) @Max(100)
    private Integer overall;

    private String strengths;
    private String weaknesses;
    private String suggestions;
}
