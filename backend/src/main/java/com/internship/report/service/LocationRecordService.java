package com.internship.report.service;

import com.internship.report.dto.*;
import com.internship.report.entity.*;
import com.internship.report.exception.BusinessException;
import com.internship.report.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationRecordService {

    private final LocationRecordRepository locationRecordRepository;
    private final UserRepository userRepository;
    private final InternshipRepository internshipRepository;

    @Transactional
    public LocationRecordDTO createRecord(Long studentId, LocationCreateRequest request) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("学生不存在"));

        if (student.getRole() != User.Role.STUDENT) {
            throw new BusinessException("只有学生可以上报位置");
        }

        LocationRecord record = new LocationRecord();
        record.setStudent(student);
        record.setLongitude(request.getLongitude());
        record.setLatitude(request.getLatitude());
        record.setAddress(request.getAddress());
        record.setCity(request.getCity());
        record.setProvince(request.getProvince());
        record.setRecordType(request.getRecordType() != null ? request.getRecordType() : "AUTO");
        record.setRemark(request.getRemark());

        if (request.getInternshipId() != null) {
            Internship internship = internshipRepository.findById(request.getInternshipId())
                    .orElse(null);
            record.setInternship(internship);
        }

        record = locationRecordRepository.save(record);
        return convertToDTO(record);
    }

    public LocationRecordDTO getLatestRecord(Long studentId) {
        LocationRecord record = locationRecordRepository.findLatestByStudentId(studentId);
        if (record == null) return null;
        return convertToDTO(record);
    }

    public List<LocationRecordDTO> getStudentRecords(Long studentId) {
        return locationRecordRepository.findByStudentIdOrderByRecordTimeDesc(studentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<LocationRecordDTO> getStudentRecordsPage(Long studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("recordTime").descending());
        return locationRecordRepository.findByStudentId(studentId, pageable)
                .map(this::convertToDTO);
    }

    public Page<LocationRecordDTO> getTeacherRecords(Long teacherId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("recordTime").descending());
        return locationRecordRepository.findByTeacherId(teacherId, pageable)
                .map(this::convertToDTO);
    }

    public Page<LocationRecordDTO> searchRecords(LocationQueryRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("recordTime").descending());

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (request.getStartTime() != null && !request.getStartTime().isEmpty()) {
            startTime = LocalDateTime.parse(request.getStartTime(), formatter);
        }
        if (request.getEndTime() != null && !request.getEndTime().isEmpty()) {
            endTime = LocalDateTime.parse(request.getEndTime(), formatter);
        }

        return locationRecordRepository.search(
                request.getStudentId(),
                request.getCity(),
                startTime,
                endTime,
                pageable
        ).map(this::convertToDTO);
    }

    private LocationRecordDTO convertToDTO(LocationRecord record) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 兼容获取企业名称：先查 companyName 冗余字段，再查 company 实体
        String companyName = null;
        if (record.getInternship() != null) {
            companyName = record.getInternship().getCompanyName();
            if (companyName == null && record.getInternship().getCompany() != null) {
                companyName = record.getInternship().getCompany().getName();
            }
        }

        return LocationRecordDTO.builder()
                .id(record.getId())
                .studentId(record.getStudent() != null ? record.getStudent().getId() : null)
                .studentName(record.getStudent() != null ? record.getStudent().getRealName() : null)
                .studentNo(record.getStudent() != null ? record.getStudent().getStudentNo() : null)
                .internshipId(record.getInternship() != null ? record.getInternship().getId() : null)
                .company(companyName)
                .longitude(record.getLongitude())
                .latitude(record.getLatitude())
                .address(record.getAddress())
                .city(record.getCity())
                .province(record.getProvince())
                .recordType(record.getRecordType())
                .remark(record.getRemark())
                .recordTime(record.getRecordTime() != null ? record.getRecordTime().format(formatter) : null)
                .build();
    }
}
