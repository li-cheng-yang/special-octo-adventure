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
public class InternshipDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private Long teacherId;
    private String teacherName;
    private Long companyId;
    private String companyName;
    private String position;
    private String address;
    private String companyContact;
    private String companyPhone;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String description;
    private String workContent;
    private String teacherComment;
    private String createTime;
    private String updateTime;
}
