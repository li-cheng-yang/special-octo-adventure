package com.internship.report.repository;

import com.internship.report.entity.LocationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LocationRecordRepository extends JpaRepository<LocationRecord, Long> {

    List<LocationRecord> findByStudentIdOrderByRecordTimeDesc(Long studentId);

    Page<LocationRecord> findByStudentId(Long studentId, Pageable pageable);

    @Query("SELECT lr FROM LocationRecord lr WHERE lr.student.id IN " +
           "(SELECT i.student.id FROM Internship i WHERE i.teacher.id = :teacherId) " +
           "AND lr.student.role = 'STUDENT'")
    Page<LocationRecord> findByTeacherId(@Param("teacherId") Long teacherId, Pageable pageable);

    @Query("SELECT lr FROM LocationRecord lr WHERE " +
           "(:studentId IS NULL OR lr.student.id = :studentId) AND " +
           "(:city IS NULL OR lr.city = :city) AND " +
           "(:startTime IS NULL OR lr.recordTime >= :startTime) AND " +
           "(:endTime IS NULL OR lr.recordTime <= :endTime)")
    Page<LocationRecord> search(@Param("studentId") Long studentId,
                                @Param("city") String city,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime,
                                Pageable pageable);

    @Query("SELECT lr.city, COUNT(lr) FROM LocationRecord lr WHERE lr.recordTime >= :since GROUP BY lr.city")
    List<Object[]> countByCitySince(@Param("since") LocalDateTime since);

    @Query(value = "SELECT * FROM location_records WHERE student_id = :studentId ORDER BY record_time DESC LIMIT 1", nativeQuery = true)
    LocationRecord findLatestByStudentId(@Param("studentId") Long studentId);


    boolean existsByStudentIdAndRecordTimeBetween(Long studentId, LocalDateTime start, LocalDateTime end);
}
