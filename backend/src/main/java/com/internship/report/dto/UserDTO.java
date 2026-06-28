package com.internship.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String role;
    private String studentNo;
    private String department;
    private String major;
    private String className;
    private String avatar;
    private String status;
    private String createTime;
}
