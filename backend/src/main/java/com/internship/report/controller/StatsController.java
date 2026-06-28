package com.internship.report.controller;

import com.internship.report.dto.ApiResponse;
import com.internship.report.entity.Internship;
import com.internship.report.entity.Report;
import com.internship.report.entity.User;
import com.internship.report.repository.InternshipRepository;
import com.internship.report.repository.ReportRepository;
import com.internship.report.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final UserRepository userRepository;
    private final InternshipRepository internshipRepository;
    private final ReportRepository reportRepository;

    /**
     * 仪表盘数据 - 需登录，根据角色返回不同范围的数据
     */
    @GetMapping("/overview")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverview(
            @AuthenticationPrincipal Long userId) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.error(401, "未登录"));
        }

        Map<String, Object> stats = new HashMap<>();

        // 基础统计 - 所有角色都能看
        stats.put("totalStudents", userRepository.countByRole(User.Role.STUDENT));
        stats.put("totalTeachers", userRepository.countByRole(User.Role.TEACHER));
        stats.put("totalAdmins", userRepository.countByRole(User.Role.ADMIN));

        // 根据角色过滤实习数据
        if (user.getRole() == User.Role.STUDENT) {
            stats.put("ongoingInternships", internshipRepository.countByStudentIdAndStatus(userId, Internship.Status.ONGOING));
            stats.put("pendingInternships", internshipRepository.countByStudentIdAndStatus(userId, Internship.Status.PENDING));
            stats.put("completedInternships", internshipRepository.countByStudentIdAndStatus(userId, Internship.Status.COMPLETED));
            stats.put("draftInternships", internshipRepository.countByStudentIdAndStatus(userId, Internship.Status.DRAFT));
        } else if (user.getRole() == User.Role.TEACHER) {
            stats.put("ongoingInternships", internshipRepository.countByTeacherIdAndStatus(userId, Internship.Status.ONGOING));
            stats.put("pendingInternships", internshipRepository.countByTeacherIdAndStatus(userId, Internship.Status.PENDING));
            stats.put("completedInternships", internshipRepository.countByTeacherIdAndStatus(userId, Internship.Status.COMPLETED));
            stats.put("draftInternships", internshipRepository.countByTeacherIdAndStatus(userId, Internship.Status.DRAFT));
        } else {
            // 管理员看全部
            stats.put("ongoingInternships", internshipRepository.countByStatus(Internship.Status.ONGOING));
            stats.put("pendingInternships", internshipRepository.countByStatus(Internship.Status.PENDING));
            stats.put("completedInternships", internshipRepository.countByStatus(Internship.Status.COMPLETED));
            stats.put("draftInternships", internshipRepository.countByStatus(Internship.Status.DRAFT));
        }

        // 汇报统计 - 根据角色过滤
        if (user.getRole() == User.Role.TEACHER) {
            stats.put("submittedReports", reportRepository.countByTeacherIdAndStatus(userId, Report.Status.SUBMITTED));
            stats.put("reviewedReports", reportRepository.countByTeacherIdAndStatus(userId, Report.Status.REVIEWED));
            stats.put("returnedReports", reportRepository.countByTeacherIdAndStatus(userId, Report.Status.RETURNED));
            stats.put("draftReports", reportRepository.countByTeacherIdAndStatus(userId, Report.Status.DRAFT));
        } else if (user.getRole() == User.Role.STUDENT) {
            stats.put("submittedReports", reportRepository.countByStudentIdAndStatus(userId, Report.Status.SUBMITTED));
            stats.put("reviewedReports", reportRepository.countByStudentIdAndStatus(userId, Report.Status.REVIEWED));
            stats.put("returnedReports", reportRepository.countByStudentIdAndStatus(userId, Report.Status.RETURNED));
            stats.put("draftReports", reportRepository.countByStudentIdAndStatus(userId, Report.Status.DRAFT));
        } else {
            stats.put("submittedReports", reportRepository.countByStatus(Report.Status.SUBMITTED));
            stats.put("reviewedReports", reportRepository.countByStatus(Report.Status.REVIEWED));
            stats.put("returnedReports", reportRepository.countByStatus(Report.Status.RETURNED));
            stats.put("draftReports", reportRepository.countByStatus(Report.Status.DRAFT));
        }

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
