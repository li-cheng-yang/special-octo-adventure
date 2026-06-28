package com.internship.report.controller;

import com.internship.report.dto.ApiResponse;
import com.internship.report.entity.Attachment;
import com.internship.report.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @AuthenticationPrincipal Long userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long reportId,
            @RequestParam(required = false) Long internshipId,
            @RequestParam(defaultValue = "OTHER") String type) {

        Attachment.AttachmentType attachmentType = Attachment.AttachmentType.valueOf(type.toUpperCase());
        Attachment attachment = fileService.uploadFile(file, userId, reportId, internshipId, attachmentType);

        FileUploadResponse response = new FileUploadResponse(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getFileSize(),
                attachment.getFileType(),
                attachment.getStoredName()
        );

        return ResponseEntity.ok(ApiResponse.success("上传成功", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Attachment attachment = fileService.getAttachment(id);
        byte[] content = fileService.getFileContent(id);

        ByteArrayResource resource = new ByteArrayResource(content);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + attachment.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(attachment.getFileType() != null ? 
                        attachment.getFileType() : "application/octet-stream"))
                .contentLength(content.length)
                .body(resource);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewFile(@PathVariable Long id) {
        Attachment attachment = fileService.getAttachment(id);
        byte[] content = fileService.getFileContent(id);

        ByteArrayResource resource = new ByteArrayResource(content);

        MediaType mediaType = getMediaType(attachment.getFileName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + attachment.getFileName() + "\"")
                .contentType(mediaType)
                .contentLength(content.length)
                .body(resource);
    }

    @GetMapping("/avatar/{filename:.+}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(fileService.getUploadDir(), "avatars", filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = fileService.determineContentType(filename);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/report/{reportId}")
    public ResponseEntity<ApiResponse<List<FileUploadResponse>>> getReportFiles(@PathVariable Long reportId) {
        List<Attachment> attachments = fileService.getReportAttachments(reportId);
        List<FileUploadResponse> response = attachments.stream()
                .map(a -> new FileUploadResponse(a.getId(), a.getFileName(), a.getFileSize(), a.getFileType(), a.getStoredName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/internship/{internshipId}")
    public ResponseEntity<ApiResponse<List<FileUploadResponse>>> getInternshipFiles(@PathVariable Long internshipId) {
        List<Attachment> attachments = fileService.getInternshipAttachments(internshipId);
        List<FileUploadResponse> response = attachments.stream()
                .map(a -> new FileUploadResponse(a.getId(), a.getFileName(), a.getFileSize(), a.getFileType(), a.getStoredName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        fileService.deleteAttachment(id, userId);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSupportedFileTypes() {
        Map<String, Object> types = new HashMap<>();
        types.put("image", List.of("image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"));
        types.put("document", List.of("application/pdf", "application/msword", 
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "text/plain"));
        types.put("maxSize", 50 * 1024 * 1024);
        return ResponseEntity.ok(ApiResponse.success(types));
    }

    private MediaType getMediaType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return MediaType.APPLICATION_PDF;
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    public record FileUploadResponse(Long id, String fileName, Long fileSize, String fileType, String storedName) {}
}
