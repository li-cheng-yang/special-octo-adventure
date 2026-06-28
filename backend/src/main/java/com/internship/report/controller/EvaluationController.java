package com.internship.report.controller;

import com.internship.report.dto.ApiResponse;
import com.internship.report.dto.EvaluationCreateRequest;
import com.internship.report.dto.EvaluationDTO;
import com.internship.report.service.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping
    public ResponseEntity<ApiResponse<EvaluationDTO>> createEvaluation(
            @AuthenticationPrincipal Long evaluatorId,
            @Valid @RequestBody EvaluationCreateRequest request) {
        EvaluationDTO dto = evaluationService.createEvaluation(evaluatorId, request);
        return ResponseEntity.ok(ApiResponse.success("评价提交成功", dto));
    }

    @GetMapping("/student")
    public ResponseEntity<ApiResponse<List<EvaluationDTO>>> getStudentEvaluations(
            @AuthenticationPrincipal Long studentId) {
        List<EvaluationDTO> list = evaluationService.getEvaluationsByStudentId(studentId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/internship/{internshipId}")
    public ResponseEntity<ApiResponse<List<EvaluationDTO>>> getInternshipEvaluations(
            @PathVariable Long internshipId) {
        List<EvaluationDTO> list = evaluationService.getEvaluationsByInternshipId(internshipId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
