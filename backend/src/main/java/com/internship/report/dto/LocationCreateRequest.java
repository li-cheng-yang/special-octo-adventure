package com.internship.report.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LocationCreateRequest {
    @NotNull(message = "经度不能为空")
    private BigDecimal longitude;

    @NotNull(message = "纬度不能为空")
    private BigDecimal latitude;

    private String address;
    private String city;
    private String province;
    private String recordType;
    private String remark;
    private Long internshipId;
}
