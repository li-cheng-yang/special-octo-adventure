package com.internship.report;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户API测试 - 需要Token认证
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    @DisplayName("TC-006: 未授权访问返回403")
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
        System.out.println("[PASS] 未授权访问被正确拒绝(403)");
    }

    @Test
    @Order(2)
    @DisplayName("TC-007: 获取当前用户信息(学生Token)")
    void testGetCurrentUserWithStudentToken() throws Exception {
        String token = AuthControllerTest.studentToken;
        if (token == null) {
            System.out.println("[SKIP] 跳过: 请先运行AuthControllerTest获取Token");
            return;
        }

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("student1"))
                .andExpect(jsonPath("$.data.role").value("STUDENT"));
        System.out.println("[PASS] 获取当前用户信息成功");
    }

    @Test
    @Order(3)
    @DisplayName("TC-008: 获取用户列表(管理员Token)")
    void testGetUserListWithAdminToken() throws Exception {
        String token = AuthControllerTest.adminToken;
        if (token == null) {
            System.out.println("[SKIP] 跳过: 请先运行AuthControllerTest获取Token");
            return;
        }

        mockMvc.perform(get("/api/users?page=0&size=10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
        System.out.println("[PASS] 管理员获取用户列表成功");
    }

    @Test
    @Order(4)
    @DisplayName("TC-009: 获取导师列表")
    void testGetTeacherList() throws Exception {
        String token = AuthControllerTest.studentToken;
        if (token == null) {
            System.out.println("[SKIP] 跳过: 请先运行AuthControllerTest获取Token");
            return;
        }

        mockMvc.perform(get("/api/users/teachers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
        System.out.println("[PASS] 获取导师列表成功");
    }
}
