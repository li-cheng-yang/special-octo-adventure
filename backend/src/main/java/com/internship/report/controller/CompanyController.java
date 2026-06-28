package com.internship.report.controller;

import com.internship.report.dto.*;
import com.internship.report.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyDTO>> createCompany(
            @Valid @RequestBody CompanyCreateRequest request) {
        CompanyDTO company = companyService.createCompany(request);
        return ResponseEntity.ok(ApiResponse.success("创建成功", company));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyDTO>> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyUpdateRequest request) {
        CompanyDTO company = companyService.updateCompany(id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", company));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyDTO>> getCompany(@PathVariable Long id) {
        CompanyDTO company = companyService.getCompany(id);
        return ResponseEntity.ok(ApiResponse.success(company));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<CompanyDTO>>> searchCompanies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageResult = companyService.searchCompanies(keyword, status, page, size);
        PageResponse<CompanyDTO> response = new PageResponse<>(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getNumber()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CompanyDTO>>> getActiveCompanies() {
        List<CompanyDTO> companies = companyService.getActiveCompanies();
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    public record PageResponse<T>(List<T> content, long totalElements, int totalPages, int currentPage) {}
}
