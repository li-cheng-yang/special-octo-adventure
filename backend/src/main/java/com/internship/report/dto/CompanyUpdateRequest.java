package com.internship.report.dto;

import lombok.Data;

@Data
public class CompanyUpdateRequest {
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
}
