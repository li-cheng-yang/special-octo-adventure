package com.internship.report.service;

import com.internship.report.entity.LocationRecord;
import com.internship.report.entity.Report;
import com.internship.report.repository.LocationRecordRepository;
import com.internship.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final LocationRecordRepository locationRepository;
    private final ReportRepository reportRepository;

    /** 安全获取字符串值（处理枚举类型） */
    private String safeStr(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    public byte[] exportCheckinsToExcel(Long userId) {
        List<LocationRecord> records = locationRepository.findByStudentIdOrderByRecordTimeDesc(userId);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("签到记录");

            // Header
            Row header = sheet.createRow(0);
            String[] headers = {"序号", "打卡类型", "经度", "纬度", "地址", "城市", "省份", "打卡时间", "备注"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Data
            int rowNum = 1;
            for (LocationRecord r : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(safeStr(r.getRecordType()));
                row.createCell(2).setCellValue(r.getLongitude() != null ? r.getLongitude().doubleValue() : 0);
                row.createCell(3).setCellValue(r.getLatitude() != null ? r.getLatitude().doubleValue() : 0);
                row.createCell(4).setCellValue(safeStr(r.getAddress()));
                row.createCell(5).setCellValue(safeStr(r.getCity()));
                row.createCell(6).setCellValue(safeStr(r.getProvince()));
                row.createCell(7).setCellValue(safeStr(r.getRecordTime()));
                row.createCell(8).setCellValue(safeStr(r.getRemark()));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败: " + e.getMessage(), e);
        }
    }

    public byte[] exportReportsToExcel(Long userId) {
        List<Report> reports = reportRepository.findByStudentIdOrderByReportDateDesc(userId);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("汇报记录");

            Row header = sheet.createRow(0);
            String[] headers = {"序号", "标题", "类型", "周次", "状态", "评分", "批阅意见", "汇报日期"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rowNum = 1;
            for (Report r : reports) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(safeStr(r.getTitle()));
                row.createCell(2).setCellValue(safeStr(r.getReportType()));
                row.createCell(3).setCellValue(r.getWeekNumber() != null ? r.getWeekNumber() : 0);
                row.createCell(4).setCellValue(safeStr(r.getStatus()));
                row.createCell(5).setCellValue(r.getScore() != null ? r.getScore() : 0);
                row.createCell(6).setCellValue(safeStr(r.getReviewComment()));
                row.createCell(7).setCellValue(safeStr(r.getReportDate()));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败: " + e.getMessage(), e);
        }
    }
}
