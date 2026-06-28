package com.internship.report.repository;

import com.internship.report.entity.Internship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long> {

    List<Internship> findByStudentId(Long studentId);

    Page<Internship> findByStudentId(Long studentId, Pageable pageable);

    Page<Internship> findByTeacherId(Long teacherId, Pageable pageable);

    List<Internship> findByTeacherId(Long teacherId);

    Optional<Internship> findByIdAndStudentId(Long id, Long studentId);

    @Query("SELECT i FROM Internship i WHERE i.student.id = :studentId AND i.status = :status")
    List<Internship> findByStudentIdAndStatus(@Param("studentId") Long studentId,
                                               @Param("status") Internship.Status status);

    @Query("SELECT i FROM Internship i WHERE " +
           "(:studentId IS NULL OR i.student.id = :studentId) AND " +
           "(:teacherId IS NULL OR i.teacher.id = :teacherId) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:companyName IS NULL OR i.companyName LIKE %:companyName%)")
    Page<Internship> search(@Param("studentId") Long studentId,
                            @Param("teacherId") Long teacherId,
                            @Param("status") Internship.Status status,
                            @Param("companyName") String companyName,
                            Pageable pageable);

    @Query("SELECT COUNT(i) FROM Internship i WHERE i.status = :status")
    long countByStatus(@Param("status") Internship.Status status);

    @Query("SELECT COUNT(i) FROM Internship i WHERE i.student.id = :studentId AND i.status = :status")
    long countByStudentIdAndStatus(@Param("studentId") Long studentId,
                                    @Param("status") Internship.Status status);

    @Query("SELECT COUNT(i) FROM Internship i WHERE i.teacher.id = :teacherId AND i.status = :status")
    long countByTeacherIdAndStatus(@Param("teacherId") Long teacherId,
                                    @Param("status") Internship.Status status);

    @Query("SELECT COUNT(DISTINCT i.student.id) FROM Internship i")
    long countDistinctStudents();


    List<Internship> findByStatus(Internship.Status status);
}
