package com.internship.report.service;

import com.internship.report.dto.*;
import com.internship.report.entity.Company;
import com.internship.report.entity.Internship;
import com.internship.report.entity.User;
import com.internship.report.exception.BusinessException;
import com.internship.report.repository.CompanyRepository;
import com.internship.report.repository.InternshipRepository;
import com.internship.report.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InternshipService {

    private final InternshipRepository internshipRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public InternshipDTO createInternship(Long studentId, InternshipCreateRequest request) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("学生不存在"));

        if (student.getRole() != User.Role.STUDENT) {
            throw new BusinessException("只有学生可以创建实习信息");
        }

        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new BusinessException("指导老师不存在"));

        if (teacher.getRole() != User.Role.TEACHER) {
            throw new BusinessException("指定的用户不是老师");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("结束日期不能早于开始日期");
        }

        Internship internship = new Internship();
        internship.setStudent(student);
        internship.setTeacher(teacher);
        internship.setStatus(Internship.Status.DRAFT);

        // 兼容处理企业信息：优先用 companyId，否则用 company 名称匹配
        if (request.getCompanyId() != null) {
            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new BusinessException("企业不存在"));
            internship.setCompany(company);
            internship.setCompanyName(company.getName());
        } else if (request.getCompany() != null && !request.getCompany().isEmpty()) {
            // 按名称查找是否已有企业
            Company company = companyRepository.findByName(request.getCompany()).orElse(null);
            if (company != null) {
                internship.setCompany(company);
                internship.setCompanyName(company.getName());
            } else {
                // 企业不在库中，只设置名称字段
                internship.setCompanyName(request.getCompany());
            }
        }

        internship.setPosition(request.getPosition());
        internship.setAddress(request.getAddress());
        internship.setCompanyContact(request.getCompanyContact());
        internship.setCompanyPhone(request.getCompanyPhone());
        internship.setStartDate(request.getStartDate());
        internship.setEndDate(request.getEndDate());
        internship.setDescription(request.getDescription());
        internship.setWorkContent(request.getWorkContent());

        internship = internshipRepository.save(internship);
        return convertToDTO(internship);
    }

    @Transactional
    public InternshipDTO updateInternship(Long internshipId, Long studentId, InternshipUpdateRequest request) {
        Internship internship = internshipRepository.findByIdAndStudentId(internshipId, studentId)
                .orElseThrow(() -> new BusinessException("实习信息不存在或无权限修改"));

        if (internship.getStatus() == Internship.Status.ONGOING ||
            internship.getStatus() == Internship.Status.COMPLETED) {
            throw new BusinessException("当前状态不允许修改");
        }

        if (request.getTeacherId() != null) {
            User teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new BusinessException("指导老师不存在"));
            if (teacher.getRole() != User.Role.TEACHER) {
                throw new BusinessException("指定的用户不是老师");
            }
            internship.setTeacher(teacher);
        }

        // 兼容处理企业信息更新
        if (request.getCompanyId() != null) {
            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new BusinessException("企业不存在"));
            internship.setCompany(company);
            internship.setCompanyName(company.getName());
        } else if (request.getCompany() != null && !request.getCompany().isEmpty()) {
            Company company = companyRepository.findByName(request.getCompany()).orElse(null);
            if (company != null) {
                internship.setCompany(company);
                internship.setCompanyName(company.getName());
            } else {
                internship.setCompany(null);
                internship.setCompanyName(request.getCompany());
            }
        }

        if (request.getPosition() != null) internship.setPosition(request.getPosition());
        if (request.getAddress() != null) internship.setAddress(request.getAddress());
        if (request.getCompanyContact() != null) internship.setCompanyContact(request.getCompanyContact());
        if (request.getCompanyPhone() != null) internship.setCompanyPhone(request.getCompanyPhone());
        if (request.getStartDate() != null) internship.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) internship.setEndDate(request.getEndDate());
        if (request.getDescription() != null) internship.setDescription(request.getDescription());
        if (request.getWorkContent() != null) internship.setWorkContent(request.getWorkContent());

        internship = internshipRepository.save(internship);
        return convertToDTO(internship);
    }

    @Transactional
    public void submitInternship(Long internshipId, Long studentId) {
        Internship internship = internshipRepository.findByIdAndStudentId(internshipId, studentId)
                .orElseThrow(() -> new BusinessException("实习信息不存在或无权限提交"));

        if (internship.getStatus() != Internship.Status.DRAFT &&
            internship.getStatus() != Internship.Status.REJECTED) {
            throw new BusinessException("当前状态不允许提交");
        }

        internship.setStatus(Internship.Status.PENDING);
        internshipRepository.save(internship);
    }

    @Transactional
    public void reviewInternship(Long internshipId, Long teacherId, InternshipReviewRequest request) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new BusinessException("实习信息不存在"));

        if (internship.getStatus() != Internship.Status.PENDING) {
            throw new BusinessException("当前状态不允许审核");
        }

        if (!internship.getTeacher().getId().equals(teacherId)) {
            throw new BusinessException("无权限审核此实习信息");
        }

        internship.setTeacherComment(request.getComment());
        internship.setStatus(request.getApproved() ? Internship.Status.ONGOING : Internship.Status.REJECTED);
        internshipRepository.save(internship);
    }

    @Transactional
    public void completeInternship(Long internshipId, Long studentId) {
        Internship internship = internshipRepository.findByIdAndStudentId(internshipId, studentId)
                .orElseThrow(() -> new BusinessException("实习信息不存在或无权限操作"));

        if (internship.getStatus() != Internship.Status.ONGOING) {
            throw new BusinessException("当前状态不允许完成");
        }

        internship.setStatus(Internship.Status.COMPLETED);
        internshipRepository.save(internship);
    }

    public InternshipDTO getInternship(Long internshipId) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new BusinessException("实习信息不存在"));
        return convertToDTO(internship);
    }

    public Page<InternshipDTO> getStudentInternships(Long studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        return internshipRepository.findByStudentId(studentId, pageable)
                .map(this::convertToDTO);
    }

    public Page<InternshipDTO> getTeacherInternships(Long teacherId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        return internshipRepository.findByTeacherId(teacherId, pageable)
                .map(this::convertToDTO);
    }

    public Page<InternshipDTO> searchInternships(InternshipQueryRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
                Sort.by("createTime").descending());

        Internship.Status status = null;
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            try {
                status = Internship.Status.valueOf(request.getStatus());
            } catch (IllegalArgumentException ignored) {}
        }

        return internshipRepository.search(
                request.getStudentId(),
                request.getTeacherId(),
                status,
                request.getCompanyName(),
                pageable
        ).map(this::convertToDTO);
    }

    public List<InternshipDTO> getAllInternships() {
        return internshipRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteInternship(Long internshipId, Long studentId) {
        Internship internship = internshipRepository.findByIdAndStudentId(internshipId, studentId)
                .orElseThrow(() -> new BusinessException("实习信息不存在或无权限删除"));

        if (internship.getStatus() == Internship.Status.ONGOING) {
            throw new BusinessException("进行中的实习不能删除");
        }

        internshipRepository.delete(internship);
    }

    public long countByStatus(Internship.Status status) {
        return internshipRepository.countByStatus(status);
    }

    public long countDistinctStudents() {
        return internshipRepository.countDistinctStudents();
    }

    private InternshipDTO convertToDTO(Internship internship) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return InternshipDTO.builder()
                .id(internship.getId())
                .studentId(internship.getStudent().getId())
                .studentName(internship.getStudent().getRealName())
                .studentNo(internship.getStudent().getStudentNo())
                .teacherId(internship.getTeacher() != null ? internship.getTeacher().getId() : null)
                .teacherName(internship.getTeacher() != null ? internship.getTeacher().getRealName() : null)
                .companyId(internship.getCompany() != null ? internship.getCompany().getId() : null)
                .companyName(internship.getCompanyName() != null ? internship.getCompanyName() :
                        (internship.getCompany() != null ? internship.getCompany().getName() : null))
                .position(internship.getPosition())
                .address(internship.getAddress())
                .companyContact(internship.getCompanyContact())
                .companyPhone(internship.getCompanyPhone())
                .startDate(internship.getStartDate())
                .endDate(internship.getEndDate())
                .status(internship.getStatus().name())
                .description(internship.getDescription())
                .workContent(internship.getWorkContent())
                .teacherComment(internship.getTeacherComment())
                .createTime(internship.getCreateTime() != null ? internship.getCreateTime().format(formatter) : null)
                .updateTime(internship.getUpdateTime() != null ? internship.getUpdateTime().format(formatter) : null)
                .build();
    }
}
