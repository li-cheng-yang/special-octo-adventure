package com.internship.report.repository;

import com.internship.report.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    
    List<Attachment> findByReportId(Long reportId);
    
    List<Attachment> findByInternshipId(Long internshipId);
    
    List<Attachment> findByUploaderId(Long uploaderId);
    
    void deleteByReportId(Long reportId);
    
    void deleteByInternshipId(Long internshipId);
}
