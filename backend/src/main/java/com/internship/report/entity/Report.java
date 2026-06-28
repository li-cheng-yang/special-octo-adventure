package com.internship.report.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_id", nullable = false)
    private Internship internship;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @Column(nullable = false, length = 100)
    private String title;  // 汇报标题
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportType reportType;  // 汇报类型
    
    @Column(columnDefinition = "TEXT")
    private String content;  // 汇报内容
    
    @Column(columnDefinition = "TEXT")
    private String summary;  // 工作总结
    
    @Column(columnDefinition = "TEXT")
    private String problem;  // 遇到的问题
    
    @Column(columnDefinition = "TEXT")
    private String solution;  // 解决方案
    
    @Column(columnDefinition = "TEXT")
    private String plan;  // 下周计划
    
    private LocalDate reportDate;  // 汇报日期
    
    private Integer weekNumber;  // 第几周
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.DRAFT;
    
    // 批阅相关
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;  // 批阅人
    
    @Column(columnDefinition = "TEXT")
    private String reviewComment;  // 批阅意见
    
    private Integer score;  // 评分 (0-100)
    
    private LocalDateTime reviewTime;  // 批阅时间
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;
    
    @UpdateTimestamp
    private LocalDateTime updateTime;
    
    public enum ReportType {
        WEEKLY,       // 周报
        MONTHLY,      // 月报
        FINAL         // 总结报告
    }
    
    public enum Status {
        DRAFT,        // 草稿
        SUBMITTED,    // 已提交
        REVIEWED,     // 已批阅
        RETURNED      // 已退回
    }
}
