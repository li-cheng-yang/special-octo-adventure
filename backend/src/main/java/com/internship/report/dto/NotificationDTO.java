package com.internship.report.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private String title;
    private String content;
    private String type;
    private String relatedType;
    private Long relatedId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
