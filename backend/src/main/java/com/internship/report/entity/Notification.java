package com.internship.report.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(length = 50)
    private String relatedType;

    @Column
    private Long relatedId;

    @Column(nullable = false)
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum NotificationType {
        INTERNSHIP_REVIEWED,    // 实习被审核
        REPORT_REVIEWED,        // 汇报被批阅
        CHECKIN_REMIND,         // 打卡提醒
        SYSTEM_NOTICE,          // 系统通知
        DEADLINE_WARNING       // 截止日期警告
    }
}
