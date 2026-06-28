package com.internship.report.controller;

import com.internship.report.dto.*;
import com.internship.report.entity.Report;
import com.internship.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    
    private final ReportService reportService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<ReportDTO>> createReport(
            @AuthenticationPrincipal Long studentId,
            @Valid @RequestBody ReportCreateRequest request) {
        ReportDTO report = reportService.createReport(studentId, request);
        return ResponseEntity.ok(ApiResponse.success("创建成功", report));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportDTO>> updateReport(
            @PathVariable Long id,
            @AuthenticationPrincipal Long studentId,
            @Valid @RequestBody ReportUpdateRequest request) {
        ReportDTO report = reportService.updateReport(id, studentId, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", report));
    }
    
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<Void>> submitReport(
            @PathVariable Long id,
            @AuthenticationPrincipal Long studentId) {
        reportService.submitReport(id, studentId);
        return ResponseEntity.ok(ApiResponse.success("提交成功", null));
    }
    
    @PostMapping("/{id}/review")
    public ResponseEntity<ApiResponse<Void>> reviewReport(
            @PathVariable Long id,
            @AuthenticationPrincipal Long teacherId,
            @Valid @RequestBody ReportReviewRequest request) {
        reportService.reviewReport(id, teacherId, request);
        return ResponseEntity.ok(ApiResponse.success("批阅成功", null));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportDTO>> getReport(@PathVariable Long id) {
        ReportDTO report = reportService.getReport(id);
        return ResponseEntity.ok(ApiResponse.success(report));
    }
    
    @GetMapping("/student")
    public ResponseEntity<ApiResponse<PageResponse<ReportDTO>>> getStudentReports(
            @AuthenticationPrincipal Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageResult = reportService.getStudentReports(studentId, page, size);
        PageResponse<ReportDTO> response = new PageResponse<>(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getNumber()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/teacher")
    public ResponseEntity<ApiResponse<PageResponse<ReportDTO>>> getTeacherReports(
            @AuthenticationPrincipal Long teacherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageResult = reportService.getTeacherReports(teacherId, page, size);
        PageResponse<ReportDTO> response = new PageResponse<>(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getNumber()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/internship/{internshipId}")
    public ResponseEntity<ApiResponse<List<ReportDTO>>> getInternshipReports(@PathVariable Long internshipId) {
        List<ReportDTO> reports = reportService.getInternshipReports(internshipId);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ReportDTO>>> searchReports(
            @ModelAttribute ReportQueryRequest request) {
        var pageResult = reportService.searchReports(request);
        PageResponse<ReportDTO> response = new PageResponse<>(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getNumber()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(
            @PathVariable Long id,
            @AuthenticationPrincipal Long studentId) {
        reportService.deleteReport(id, studentId);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
    
    @GetMapping("/score/{studentId}")
    public ResponseEntity<ApiResponse<Double>> getAverageScore(@PathVariable Long studentId) {
        Double score = reportService.getAverageScore(studentId);
        return ResponseEntity.ok(ApiResponse.success(score));
    }
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ReportStats>> getReportStats() {
        ReportStats stats = new ReportStats(
                reportService.countByStatus(Report.Status.DRAFT),
                reportService.countByStatus(Report.Status.SUBMITTED),
                reportService.countByStatus(Report.Status.REVIEWED),
                reportService.countByStatus(Report.Status.RETURNED)
        );
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    // 分页响应包装类
    public record PageResponse<T>(List<T> content, long totalElements, int totalPages, int currentPage) {}
    
    // 汇报统计
    public record ReportStats(long draftCount, long submittedCount, long reviewedCount, long returnedCount) {}
}
