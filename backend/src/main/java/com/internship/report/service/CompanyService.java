package com.internship.report.service;

import com.internship.report.dto.*;
import com.internship.report.entity.Company;
import com.internship.report.exception.BusinessException;
import com.internship.report.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyDTO createCompany(CompanyCreateRequest request) {
        if (companyRepository.existsByName(request.getName())) {
            throw new BusinessException("企业名称已存在");
        }

        Company company = new Company();
        company.setName(request.getName());
        company.setIndustry(request.getIndustry());
        company.setAddress(request.getAddress());
        company.setCity(request.getCity());
        company.setProvince(request.getProvince());
        company.setContactName(request.getContactName());
        company.setContactPhone(request.getContactPhone());
        company.setContactEmail(request.getContactEmail());
        company.setDescription(request.getDescription());
        company.setLogoUrl(request.getLogoUrl());
        company.setWebsite(request.getWebsite());
        company.setStatus(Company.Status.ACTIVE);
        company.setStudentCount(0);

        company = companyRepository.save(company);
        return convertToDTO(company);
    }

    @Transactional
    public CompanyDTO updateCompany(Long id, CompanyUpdateRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("企业不存在"));

        if (request.getName() != null) {
            if (!request.getName().equals(company.getName()) && companyRepository.existsByName(request.getName())) {
                throw new BusinessException("企业名称已存在");
            }
            company.setName(request.getName());
        }
        if (request.getIndustry() != null) company.setIndustry(request.getIndustry());
        if (request.getAddress() != null) company.setAddress(request.getAddress());
        if (request.getCity() != null) company.setCity(request.getCity());
        if (request.getProvince() != null) company.setProvince(request.getProvince());
        if (request.getContactName() != null) company.setContactName(request.getContactName());
        if (request.getContactPhone() != null) company.setContactPhone(request.getContactPhone());
        if (request.getContactEmail() != null) company.setContactEmail(request.getContactEmail());
        if (request.getDescription() != null) company.setDescription(request.getDescription());
        if (request.getLogoUrl() != null) company.setLogoUrl(request.getLogoUrl());
        if (request.getWebsite() != null) company.setWebsite(request.getWebsite());
        if (request.getStatus() != null) {
            try {
                company.setStatus(Company.Status.valueOf(request.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException("无效的企业状态");
            }
        }

        company = companyRepository.save(company);
        return convertToDTO(company);
    }

    @Transactional
    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("企业不存在"));
        if (company.getStudentCount() != null && company.getStudentCount() > 0) {
            throw new BusinessException("该企业下有关联学生，无法删除");
        }
        companyRepository.delete(company);
    }

    public CompanyDTO getCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("企业不存在"));
        return convertToDTO(company);
    }

    public Page<CompanyDTO> searchCompanies(String keyword, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Company.Status statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = Company.Status.valueOf(status);
            } catch (IllegalArgumentException ignored) {}
        }
        return companyRepository.search(keyword, statusEnum, pageable)
                .map(this::convertToDTO);
    }

    public java.util.List<CompanyDTO> getActiveCompanies() {
        return companyRepository.findByStatus(Company.Status.ACTIVE).stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    private CompanyDTO convertToDTO(Company company) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return CompanyDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .industry(company.getIndustry())
                .address(company.getAddress())
                .city(company.getCity())
                .province(company.getProvince())
                .contactName(company.getContactName())
                .contactPhone(company.getContactPhone())
                .contactEmail(company.getContactEmail())
                .description(company.getDescription())
                .logoUrl(company.getLogoUrl())
                .website(company.getWebsite())
                .status(company.getStatus().name())
                .studentCount(company.getStudentCount())
                .createTime(company.getCreateTime() != null ? company.getCreateTime().format(formatter) : null)
                .updateTime(company.getUpdateTime() != null ? company.getUpdateTime().format(formatter) : null)
                .build();
    }
}
