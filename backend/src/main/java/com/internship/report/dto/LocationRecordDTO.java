package com.internship.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationRecordDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private Long internshipId;
    private String company;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String address;
    private String city;
    private String province;
    private String recordType;
    private String remark;
    private String recordTime;
}
