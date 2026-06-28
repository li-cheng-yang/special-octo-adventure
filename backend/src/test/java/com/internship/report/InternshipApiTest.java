package com.internship.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 实习信息API测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InternshipApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    @DisplayName("TC-010: 学生获取自己的实习列表")
    void testGetStudentInternships() throws Exception {
        String token = AuthControllerTest.studentToken;
        if (token == null) {
            System.out.println("[SKIP] 跳过: 请先运行AuthControllerTest获取Token");
            return;
        }

        mockMvc.perform(get("/api/internships/student?page=0&size=10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
        System.out.println("[PASS] 学生获取实习列表成功");
    }

    @Test
    @Order(2)
    @DisplayName("TC-011: 获取实习统计信息")
    void testGetInternshipStats() throws Exception {
        String token = AuthControllerTest.studentToken;
        if (token == null) {
            System.out.println("[SKIP] 跳过: 请先运行AuthControllerTest获取Token");
            return;
        }

        mockMvc.perform(get("/api/internships/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        System.out.println("[PASS] 获取实习统计成功");
    }
}
