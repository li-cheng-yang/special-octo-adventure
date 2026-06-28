package com.internship.report.repository;

import com.internship.report.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findByStudentId(Long studentId, Pageable pageable);

    List<Report> findByInternshipId(Long internshipId);

    Page<Report> findByInternshipId(Long internshipId, Pageable pageable);

    Optional<Report> findByIdAndStudentId(Long id, Long studentId);

    @Query("SELECT r FROM Report r WHERE r.student.id IN " +
           "(SELECT i.student.id FROM Internship i WHERE i.teacher.id = :teacherId) " +
           "AND r.student.role = 'STUDENT'")
    Page<Report> findByTeacherId(@Param("teacherId") Long teacherId, Pageable pageable);

    @Query("SELECT r FROM Report r WHERE " +
           "(:studentId IS NULL OR r.student.id = :studentId) AND " +
           "(:internshipId IS NULL OR r.internship.id = :internshipId) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:reportType IS NULL OR r.reportType = :reportType) AND " +
           "(:title IS NULL OR r.title LIKE %:title%)")
    Page<Report> search(@Param("studentId") Long studentId,
                        @Param("internshipId") Long internshipId,
                        @Param("status") Report.Status status,
                        @Param("reportType") Report.ReportType reportType,
                        @Param("title") String title,
                        Pageable pageable);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = :status")
    long countByStatus(@Param("status") Report.Status status);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.student.id = :studentId AND r.status = :status")
    long countByStudentIdAndStatus(@Param("studentId") Long studentId,
                                    @Param("status") Report.Status status);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.internship.teacher.id = :teacherId AND r.status = :status")
    long countByTeacherIdAndStatus(@Param("teacherId") Long teacherId,
                                    @Param("status") Report.Status status);

    @Query("SELECT AVG(r.score) FROM Report r WHERE r.student.id = :studentId AND r.status = 'REVIEWED'")
    Double getAverageScoreByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT r FROM Report r WHERE r.student.id = :studentId AND r.reportDate BETWEEN :startDate AND :endDate")
    List<Report> findByStudentIdAndDateRange(@Param("studentId") Long studentId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    boolean existsByInternshipIdAndWeekNumber(Long internshipId, Integer weekNumber);

    List<Report> findByStudentIdOrderByReportDateDesc(Long studentId);
}
