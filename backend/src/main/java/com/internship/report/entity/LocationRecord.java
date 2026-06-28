package com.internship.report.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "location_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_id")
    private Internship internship;

    @Column(nullable = false, precision = 12, scale = 8)
    private BigDecimal longitude;

    @Column(nullable = false, precision = 12, scale = 8)
    private BigDecimal latitude;

    @Column(length = 200)
    private String address;

    @Column(length = 50)
    private String city;

    @Column(length = 50)
    private String province;

    @Column(length = 20)
    private String recordType;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime recordTime;
}
