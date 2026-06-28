package com.internship.report.dto;

import lombok.Data;

@Data
public class InternshipQueryRequest {
    private Long studentId;
    private Long teacherId;
    private String status;
    private String company;
    private String companyName;
    private Integer page = 0;
    private Integer size = 10;
}
