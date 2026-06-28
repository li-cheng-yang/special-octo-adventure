package com.internship.report.controller;

import com.internship.report.dto.*;
import com.internship.report.service.LocationRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationRecordService locationRecordService;

    @PostMapping
    public ResponseEntity<ApiResponse<LocationRecordDTO>> createRecord(
            @AuthenticationPrincipal Long studentId,
            @Valid @RequestBody LocationCreateRequest request) {
        LocationRecordDTO record = locationRecordService.createRecord(studentId, request);
        return ResponseEntity.ok(ApiResponse.success("位置上报成功", record));
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<LocationRecordDTO>> getLatestRecord(
            @AuthenticationPrincipal Long studentId) {
        LocationRecordDTO record = locationRecordService.getLatestRecord(studentId);
        return ResponseEntity.ok(ApiResponse.success(record));
    }

    @GetMapping("/student")
    public ResponseEntity<ApiResponse<List<LocationRecordDTO>>> getStudentRecords(
            @AuthenticationPrincipal Long studentId) {
        List<LocationRecordDTO> records = locationRecordService.getStudentRecords(studentId);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/student/page")
    public ResponseEntity<ApiResponse<PageResponse<LocationRecordDTO>>> getStudentRecordsPage(
            @AuthenticationPrincipal Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageResult = locationRecordService.getStudentRecordsPage(studentId, page, size);
        PageResponse<LocationRecordDTO> response = new PageResponse<>(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getNumber()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/teacher")
    public ResponseEntity<ApiResponse<PageResponse<LocationRecordDTO>>> getTeacherRecords(
            @AuthenticationPrincipal Long teacherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageResult = locationRecordService.getTeacherRecords(teacherId, page, size);
        PageResponse<LocationRecordDTO> response = new PageResponse<>(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getNumber()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<LocationRecordDTO>>> searchRecords(
            @ModelAttribute LocationQueryRequest request) {
        var pageResult = locationRecordService.searchRecords(request);
        PageResponse<LocationRecordDTO> response = new PageResponse<>(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getNumber()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    public record PageResponse<T>(List<T> content, long totalElements, int totalPages, int currentPage) {}
}
