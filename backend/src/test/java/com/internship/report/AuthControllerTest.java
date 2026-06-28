package com.internship.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.report.dto.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证模块测试
 * 测试登录功能是否正常
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    public static String studentToken;
    public static String teacherToken;
    public static String adminToken;

    @Test
    @Order(1)
    @DisplayName("TC-001: 学生登录成功")
    void testStudentLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("student1");
        request.setPassword("123456");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.username").value("student1"))
                .andExpect(jsonPath("$.data.role").value("STUDENT"))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        studentToken = objectMapper.readTree(response).path("data").path("token").asText();
        System.out.println("[PASS] 学生登录成功，Token已获取");
    }

    @Test
    @Order(2)
    @DisplayName("TC-002: 导师登录成功")
    void testTeacherLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("teacher1");
        request.setPassword("123456");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.role").value("TEACHER"))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        teacherToken = objectMapper.readTree(response).path("data").path("token").asText();
        System.out.println("[PASS] 导师登录成功");
    }

    @Test
    @Order(3)
    @DisplayName("TC-003: 管理员登录成功")
    void testAdminLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        adminToken = objectMapper.readTree(response).path("data").path("token").asText();
        System.out.println("[PASS] 管理员登录成功");
    }

    @Test
    @Order(4)
    @DisplayName("TC-004: 密码错误登录失败")
    void testWrongPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("student1");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
        System.out.println("[PASS] 错误密码被正确拒绝");
    }

    @Test
    @Order(5)
    @DisplayName("TC-005: 不存在用户登录失败")
    void testNonExistentUser() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("notexistuser");
        request.setPassword("123456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
        System.out.println("[PASS] 不存在用户被正确拒绝");
    }
}
