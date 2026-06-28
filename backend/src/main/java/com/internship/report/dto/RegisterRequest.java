package com.internship.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少6位")
    private String password;
    
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    private String email;
    
    private String phone;
    
    @NotBlank(message = "角色不能为空")
    private String role;
    
    private String studentNo;
    
    private String department;
    
    private String major;
    
    private String className;
}
