package com.internship.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDTO {
    private Long id;
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
    private String status;
    private Integer studentCount;
    private String createTime;
    private String updateTime;
}
