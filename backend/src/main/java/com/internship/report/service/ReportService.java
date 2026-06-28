package com.internship.report.service;

import com.internship.report.dto.*;
import com.internship.report.entity.Internship;
import com.internship.report.entity.Report;
import com.internship.report.entity.User;
import com.internship.report.exception.BusinessException;
import com.internship.report.repository.InternshipRepository;
import com.internship.report.repository.ReportRepository;
import com.internship.report.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final InternshipRepository internshipRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReportDTO createReport(Long studentId, ReportCreateRequest request) {
        Internship internship = internshipRepository.findById(request.getInternshipId())
                .orElseThrow(() -> new BusinessException("实习信息不存在"));

        if (!internship.getStudent().getId().equals(studentId)) {
            throw new BusinessException("无权限为此实习创建汇报");
        }

        if (internship.getStatus() != Internship.Status.ONGOING) {
            throw new BusinessException("只有进行中的实习才能提交汇报");
        }

        // 检查周报是否已存在
        if (request.getWeekNumber() != null &&
            reportRepository.existsByInternshipIdAndWeekNumber(request.getInternshipId(), request.getWeekNumber())) {
            throw new BusinessException("该周汇报已存在");
        }

        User student = userRepository.findById(studentId).orElseThrow();

        Report report = new Report();
        report.setInternship(internship);
        report.setStudent(student);
        report.setTitle(request.getTitle());
        report.setReportType(Report.ReportType.valueOf(request.getReportType().toUpperCase()));
        report.setContent(request.getContent());
        report.setSummary(request.getSummary());
        report.setProblem(request.getProblem());
        report.setSolution(request.getSolution());
        report.setPlan(request.getPlan());
        report.setReportDate(request.getReportDate());
        report.setWeekNumber(request.getWeekNumber());
        report.setStatus(Report.Status.DRAFT);

        report = reportRepository.save(report);
        return convertToDTO(report);
    }

    @Transactional
    public ReportDTO updateReport(Long reportId, Long studentId, ReportUpdateRequest request) {
        Report report = reportRepository.findByIdAndStudentId(reportId, studentId)
                .orElseThrow(() -> new BusinessException("汇报不存在或无权限修改"));

        if (report.getStatus() == Report.Status.REVIEWED) {
            throw new BusinessException("已批阅的汇报不能修改");
        }

        if (request.getTitle() != null) report.setTitle(request.getTitle());
        if (request.getReportType() != null) {
            report.setReportType(Report.ReportType.valueOf(request.getReportType().toUpperCase()));
        }
        if (request.getContent() != null) report.setContent(request.getContent());
        if (request.getSummary() != null) report.setSummary(request.getSummary());
        if (request.getProblem() != null) report.setProblem(request.getProblem());
        if (request.getSolution() != null) report.setSolution(request.getSolution());
        if (request.getPlan() != null) report.setPlan(request.getPlan());
        if (request.getReportDate() != null) report.setReportDate(request.getReportDate());
        if (request.getWeekNumber() != null) report.setWeekNumber(request.getWeekNumber());

        report = reportRepository.save(report);
        return convertToDTO(report);
    }

    @Transactional
    public void submitReport(Long reportId, Long studentId) {
        Report report = reportRepository.findByIdAndStudentId(reportId, studentId)
                .orElseThrow(() -> new BusinessException("汇报不存在或无权限提交"));

        if (report.getStatus() != Report.Status.DRAFT && report.getStatus() != Report.Status.RETURNED) {
            throw new BusinessException("当前状态不允许提交");
        }

        report.setStatus(Report.Status.SUBMITTED);
        reportRepository.save(report);
    }

    @Transactional
    public void reviewReport(Long reportId, Long teacherId, ReportReviewRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException("汇报不存在"));

        if (report.getStatus() != Report.Status.SUBMITTED) {
            throw new BusinessException("当前状态不允许批阅");
        }

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (teacher.getRole() != User.Role.TEACHER) {
            throw new BusinessException("只有导师可以批阅汇报");
        }

        // 验证是否是该学生的导师
        Internship internship = report.getInternship();
        if (!internship.getTeacher().getId().equals(teacherId)) {
            throw new BusinessException("无权限批阅此汇报");
        }

        report.setReviewer(teacher);
        report.setReviewComment(request.getComment());
        report.setScore(request.getScore());
        report.setReviewTime(LocalDateTime.now());
        report.setStatus(request.getApproved() ? Report.Status.REVIEWED : Report.Status.RETURNED);

        reportRepository.save(report);
    }

    public ReportDTO getReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException("汇报不存在"));
        return convertToDTO(report);
    }

    public Page<ReportDTO> getStudentReports(Long studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        return reportRepository.findByStudentId(studentId, pageable)
                .map(this::convertToDTO);
    }

    public Page<ReportDTO> getTeacherReports(Long teacherId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        return reportRepository.findByTeacherId(teacherId, pageable)
                .map(this::convertToDTO);
    }

    public List<ReportDTO> getInternshipReports(Long internshipId) {
        return reportRepository.findByInternshipId(internshipId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<ReportDTO> searchReports(ReportQueryRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
                Sort.by("createTime").descending());

        Report.Status status = null;
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            try {
                status = Report.Status.valueOf(request.getStatus());
            } catch (IllegalArgumentException ignored) {}
        }

        Report.ReportType reportType = null;
        if (request.getReportType() != null && !request.getReportType().isEmpty()) {
            try {
                reportType = Report.ReportType.valueOf(request.getReportType());
            } catch (IllegalArgumentException ignored) {}
        }

        return reportRepository.search(
                request.getStudentId(),
                request.getInternshipId(),
                status,
                reportType,
                request.getTitle(),
                pageable
        ).map(this::convertToDTO);
    }

    @Transactional
    public void deleteReport(Long reportId, Long studentId) {
        Report report = reportRepository.findByIdAndStudentId(reportId, studentId)
                .orElseThrow(() -> new BusinessException("汇报不存在或无权限删除"));

        if (report.getStatus() == Report.Status.REVIEWED) {
            throw new BusinessException("已批阅的汇报不能删除");
        }

        reportRepository.delete(report);
    }

    public Double getAverageScore(Long studentId) {
        return reportRepository.getAverageScoreByStudentId(studentId);
    }

    public long countByStatus(Report.Status status) {
        return reportRepository.countByStatus(status);
    }

    /**
     * 辅助方法：从 Internship 中安全获取企业名称
     * 兼容逻辑：优先用 companyName 冗余字段，其次用 company 关联实体
     */
    private String getCompanyNameFromInternship(Internship internship) {
        if (internship == null) {
            return null;
        }
        // 优先使用冗余字段 company_name（v2.0新增）
        if (internship.getCompanyName() != null && !internship.getCompanyName().isEmpty()) {
            return internship.getCompanyName();
        }
        // 其次使用关联的 Company 实体（v2.0新增）
        if (internship.getCompany() != null) {
            return internship.getCompany().getName();
        }
        return null;
    }

    private ReportDTO convertToDTO(Report report) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return ReportDTO.builder()
                .id(report.getId())
                .internshipId(report.getInternship().getId())
                .company(getCompanyNameFromInternship(report.getInternship()))  // 修复：兼容 Company 实体
                .studentId(report.getStudent().getId())
                .studentName(report.getStudent().getRealName())
                .studentNo(report.getStudent().getStudentNo())
                .title(report.getTitle())
                .reportType(report.getReportType().name())
                .content(report.getContent())
                .summary(report.getSummary())
                .problem(report.getProblem())
                .solution(report.getSolution())
                .plan(report.getPlan())
                .reportDate(report.getReportDate())
                .weekNumber(report.getWeekNumber())
                .status(report.getStatus().name())
                .reviewerId(report.getReviewer() != null ? report.getReviewer().getId() : null)
                .reviewerName(report.getReviewer() != null ? report.getReviewer().getRealName() : null)
                .reviewComment(report.getReviewComment())
                .score(report.getScore())
                .reviewTime(report.getReviewTime() != null ? report.getReviewTime().format(formatter) : null)
                .createTime(report.getCreateTime() != null ? report.getCreateTime().format(formatter) : null)
                .updateTime(report.getUpdateTime() != null ? report.getUpdateTime().format(formatter) : null)
                .build();
    }
}
