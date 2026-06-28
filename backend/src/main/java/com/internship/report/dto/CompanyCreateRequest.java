package com.internship.report.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyCreateRequest {
    @NotBlank(message = "企业名称不能为空")
    private String name;
    private String industry;
    private String address;
    private String city;
    private String province;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String description;
    private String logoUrl;
    private String website;
}
