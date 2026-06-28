package com.internship.report.controller;

import com.internship.report.dto.*;
import com.internship.report.entity.Internship;
import com.internship.report.service.InternshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/internships")
@RequiredArgsConstructor
public class InternshipController {
    
    private final InternshipService internshipService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<InternshipDTO>> createInternship(
            @AuthenticationPrincipal Long studentId,
            @Valid @RequestBody InternshipCreateRequest request) {
        InternshipDTO internship = internshipService.createInternship(studentId, request);
        return ResponseEntity.ok(ApiResponse.success("创建成功", internship));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InternshipDTO>> updateInternship(
            @PathVariable Long id,
            @AuthenticationPrincipal Long studentId,
            @Valid @RequestBody InternshipUpdateRequest request) {
        InternshipDTO internship = internshipService.updateInternship(id, studentId, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", internship));
    }
    
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<Void>> submitInternship(
            @PathVariable Long id,
            @AuthenticationPrincipal Long studentId) {
        internshipService.submitInternship(id, studentId);
        return ResponseEntity.ok(ApiResponse.success("提交成功", null));
    }
    
    @PostMapping("/{id}/review")
    public ResponseEntity<ApiResponse<Void>> reviewInternship(
            @PathVariable Long id,
            @AuthenticationPrincipal Long teacherId,
            @Valid @RequestBody InternshipReviewRequest request) {
        internshipService.reviewInternship(id, teacherId, request);
        return ResponseEntity.ok(ApiResponse.success("审核成功", null));
    }
    
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Void>> completeInternship(
            @PathVariable Long id,
            @AuthenticationPrincipal Long studentId) {
        internshipService.completeInternship(id, studentId);
        return ResponseEntity.ok(ApiResponse.success("已完成", null));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InternshipDTO>> getInternship(@PathVariable Long id) {
        InternshipDTO internship = internshipService.getInternship(id);
        return ResponseEntity.ok(ApiResponse.success(internship));
    }
    
    @GetMapping("/student")
    public ResponseEntity<ApiResponse<PageResponse<InternshipDTO>>> getStudentInternships(
            @AuthenticationPrincipal Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageResult = internshipService.getStudentInternships(studentId, page, size);
        PageResponse<InternshipDTO> response = new PageResponse<>(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getNumber()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/teacher")
    public ResponseEntity<ApiResponse<PageResponse<InternshipDTO>>> getTeacherInternships(
            @AuthenticationPrincipal Long teacherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageResult = internshipService.getTeacherInternships(teacherId, page, size);
        PageResponse<InternshipDTO> response = new PageResponse<>(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getNumber()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<InternshipDTO>>> searchInternships(
            @ModelAttribute InternshipQueryRequest request) {
        var pageResult = internshipService.searchInternships(request);
        PageResponse<InternshipDTO> response = new PageResponse<>(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getNumber()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<InternshipDTO>>> getAllInternships() {
        List<InternshipDTO> internships = internshipService.getAllInternships();
        return ResponseEntity.ok(ApiResponse.success(internships));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInternship(
            @PathVariable Long id,
            @AuthenticationPrincipal Long studentId) {
        internshipService.deleteInternship(id, studentId);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<InternshipStats>> getInternshipStats() {
        InternshipStats stats = new InternshipStats(
                internshipService.countByStatus(Internship.Status.DRAFT),
                internshipService.countByStatus(Internship.Status.PENDING),
                internshipService.countByStatus(Internship.Status.ONGOING),
                internshipService.countByStatus(Internship.Status.COMPLETED),
                internshipService.countDistinctStudents()
        );
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    // 分页响应包装类
    public record PageResponse<T>(List<T> content, long totalElements, int totalPages, int currentPage) {}
    
    // 实习统计
    public record InternshipStats(long draftCount, long pendingCount, long ongoingCount, 
                                   long completedCount, long studentCount) {}
}
