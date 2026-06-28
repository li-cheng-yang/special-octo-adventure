package com.internship.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InternshipCreateRequest {
    @NotNull(message = "指导老师ID不能为空")
    private Long teacherId;

    private Long companyId;  // v2.0 新增：优先使用企业ID关联

    @NotBlank(message = "实习单位不能为空")
    private String company;

    private String position;
    private String address;
    private String companyContact;
    private String companyPhone;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    private String description;
    private String workContent;
}
