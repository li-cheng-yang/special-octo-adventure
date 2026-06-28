package com.internship.report.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Report report;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_id")
    private Internship internship;
    
    @Column(nullable = false)
    private Long uploaderId;  // 上传者ID
    
    @Column(nullable = false, length = 200)
    private String fileName;  // 原始文件名
    
    @Column(nullable = false, length = 200)
    private String storedName;  // 存储文件名
    
    @Column(nullable = false, length = 500)
    private String filePath;  // 文件路径
    
    @Column(nullable = false)
    private Long fileSize;  // 文件大小(字节)
    
    @Column(length = 50)
    private String fileType;  // 文件类型
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttachmentType attachmentType = AttachmentType.OTHER;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime uploadTime;
    
    public enum AttachmentType {
        REPORT,       // 汇报附件
        INTERNSHIP,   // 实习证明
        CERTIFICATE,  // 证书
        OTHER         // 其他
    }
}
