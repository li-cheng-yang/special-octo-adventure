package com.internship.report.controller;

import com.internship.report.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/checkins/excel")
    public ResponseEntity<byte[]> exportCheckinsToExcel(@AuthenticationPrincipal Long userId) {
        byte[] excelData = exportService.exportCheckinsToExcel(userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=checkin_records.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    @GetMapping("/reports/excel")
    public ResponseEntity<byte[]> exportReportsToExcel(@AuthenticationPrincipal Long userId) {
        byte[] excelData = exportService.exportReportsToExcel(userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reports.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }
}
