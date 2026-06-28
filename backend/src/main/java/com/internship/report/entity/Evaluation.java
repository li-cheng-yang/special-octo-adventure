package com.internship.report.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "evaluations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_id", nullable = false)
    private Internship internship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private EvaluatorType evaluatorType;

    @Column(nullable = false)
    private Integer workPerformance;

    @Column(nullable = false)
    private Integer learningAbility;

    @Column(nullable = false)
    private Integer communication;

    @Column(nullable = false)
    private Integer punctuality;

    @Column(nullable = false)
    private Integer overall;

    @Column(length = 1000)
    private String strengths;

    @Column(length = 1000)
    private String weaknesses;

    @Column(length = 1000)
    private String suggestions;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum EvaluatorType {
        TEACHER,    // 导师评价
        COMPANY     // 企业评价
    }
}
