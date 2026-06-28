package com.internship.report.repository;

import com.internship.report.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    List<Evaluation> findByInternshipIdOrderByCreatedAtDesc(Long internshipId);

    List<Evaluation> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    @Query("SELECT e FROM Evaluation e WHERE e.internship.id = :internshipId AND e.evaluatorType = :type")
    Optional<Evaluation> findByInternshipIdAndEvaluatorType(@Param("internshipId") Long internshipId,
                                                              @Param("type") Evaluation.EvaluatorType type);

    @Query("SELECT AVG(e.overall) FROM Evaluation e WHERE e.student.id = :studentId")
    Double getAverageScoreByStudentId(@Param("studentId") Long studentId);
}
