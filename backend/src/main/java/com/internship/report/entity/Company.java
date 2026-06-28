package com.internship.report.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(length = 50)
    private String industry;
    
    @Column(length = 200)
    private String address;
    
    @Column(length = 20)
    private String city;
    
    @Column(length = 20)
    private String province;
    
    @Column(length = 50)
    private String contactName;
    
    @Column(length = 20)
    private String contactPhone;
    
    @Column(length = 100)
    private String contactEmail;
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 255)
    private String logoUrl;
    
    @Column(length = 100)
    private String website;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;
    
    @Column(nullable = false)
    private Integer studentCount = 0;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;
    
    @UpdateTimestamp
    private LocalDateTime updateTime;
    
    public enum Status {
        ACTIVE,       // 正常合作
        INACTIVE,     // 暂停合作
        BLACKLISTED   // 黑名单
    }
}
