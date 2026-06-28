package com.internship.report.dto;

import lombok.Data;

@Data
public class LocationQueryRequest {
    private Long studentId;
    private String city;
    private String startTime;
    private String endTime;
    private int page = 0;
    private int size = 10;
}
