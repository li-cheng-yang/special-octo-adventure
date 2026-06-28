package com.internship.report.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class InternshipUpdateRequest {
    private Long teacherId;
    private Long companyId;
    private String company;
    private String position;
    private String address;
    private String companyContact;
    private String companyPhone;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private String workContent;
    private String status;
}
