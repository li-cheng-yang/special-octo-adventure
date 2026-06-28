package com.internship.report.controller;

import com.internship.report.dto.ApiResponse;
import com.internship.report.entity.Internship;
import com.internship.report.entity.User;
import com.internship.report.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final InternshipRepository internshipRepository;
    private final ReportRepository reportRepository;
    private final LocationRecordRepository locationRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", userRepository.countByRole(User.Role.STUDENT));
        stats.put("totalTeachers", userRepository.countByRole(User.Role.TEACHER));
        stats.put("totalInternships", internshipRepository.count());
        stats.put("totalReports", reportRepository.count());
        stats.put("totalCompanies", companyRepository.count());
        stats.put("totalCheckins", locationRepository.count());
        stats.put("pendingReviews", internshipRepository.countByStatus(Internship.Status.PENDING));
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/internship-distribution")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getInternshipDistribution() {
        List<Map<String, Object>> data = internshipRepository.findAll().stream()
                .collect(Collectors.groupingBy(i -> i.getStatus().name(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", translateStatus(e.getKey()));
                    map.put("value", e.getValue());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/score-trend")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getScoreTrend() {
        List<Map<String, Object>> data = reportRepository.findAll().stream()
                .filter(r -> r.getScore() != null)
                .sorted((a, b) -> a.getReportDate().compareTo(b.getReportDate()))
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", r.getReportDate().toString());
                    map.put("score", r.getScore());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/checkin-heatmap")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCheckinHeatmap() {
        List<Map<String, Object>> data = locationRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        l -> l.getRecordTime().toLocalDate().toString(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", e.getKey());
                    map.put("count", e.getValue());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/company-ranking")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCompanyRanking() {
        List<Map<String, Object>> data = internshipRepository.findAll().stream()
                .collect(Collectors.groupingBy(Internship::getCompanyName, Collectors.counting()))
                .entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("company", e.getKey());
                    map.put("count", e.getValue());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    private String translateStatus(String status) {
        return switch (status) {
            case "DRAFT" -> "草稿";
            case "PENDING" -> "待审核";
            case "ONGOING" -> "进行中";
            case "COMPLETED" -> "已完成";
            case "REJECTED" -> "已驳回";
            default -> status;
        };
    }
}
