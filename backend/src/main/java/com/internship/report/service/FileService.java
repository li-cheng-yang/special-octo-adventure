package com.internship.report.service;

import com.internship.report.entity.Attachment;
import com.internship.report.exception.BusinessException;
import com.internship.report.repository.AttachmentRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class FileService {

    @Value("${file.upload-dir:/tmp/uploads}")
    @Getter
    private String uploadDir;

    private final AttachmentRepository attachmentRepository;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            // Images
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp",
            // Documents
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "text/markdown",
            // Archives
            "application/zip",
            "application/x-rar-compressed"
    );

    public FileService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    @Transactional
    public Attachment uploadFile(MultipartFile file, Long uploaderId, 
                                  Long reportId, Long internshipId,
                                  Attachment.AttachmentType type) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            if (!isAllowedExtension(file.getOriginalFilename())) {
                throw new BusinessException("不支持的文件类型: " + contentType);
            }
        }

        if (file.getSize() > 50 * 1024 * 1024) {
            throw new BusinessException("文件大小不能超过50MB");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID().toString() + extension;

            Path filePath = uploadPath.resolve(storedName);
            file.transferTo(filePath.toFile());

            Attachment attachment = new Attachment();
            attachment.setUploaderId(uploaderId);
            attachment.setFileName(originalFilename);
            attachment.setStoredName(storedName);
            attachment.setFilePath(filePath.toString());
            attachment.setFileSize(file.getSize());
            attachment.setFileType(contentType);
            attachment.setAttachmentType(type);

            attachment = attachmentRepository.save(attachment);
            return attachment;

        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    public List<Attachment> getReportAttachments(Long reportId) {
        return attachmentRepository.findByReportId(reportId);
    }

    public List<Attachment> getInternshipAttachments(Long internshipId) {
        return attachmentRepository.findByInternshipId(internshipId);
    }

    public Attachment getAttachment(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BusinessException("附件不存在"));
    }

    @Transactional
    public void deleteAttachment(Long attachmentId, Long userId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BusinessException("附件不存在"));

        if (!attachment.getUploaderId().equals(userId)) {
            throw new BusinessException("无权限删除此附件");
        }

        try {
            Files.deleteIfExists(Paths.get(attachment.getFilePath()));
        } catch (IOException ignored) {}

        attachmentRepository.delete(attachment);
    }

    public byte[] getFileContent(Long attachmentId) {
        Attachment attachment = getAttachment(attachmentId);
        try {
            return Files.readAllBytes(Paths.get(attachment.getFilePath()));
        } catch (IOException e) {
            throw new BusinessException("读取文件失败");
        }
    }

    public String determineContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".pdf")) return "application/pdf";
        return "application/octet-stream";
    }

    private boolean isAllowedExtension(String filename) {
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") ||
               lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".pdf") ||
               lower.endsWith(".doc") || lower.endsWith(".docx") || lower.endsWith(".xls") ||
               lower.endsWith(".xlsx") || lower.endsWith(".ppt") || lower.endsWith(".pptx") ||
               lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".zip") ||
               lower.endsWith(".rar") || lower.endsWith(".bmp");
    }
}
