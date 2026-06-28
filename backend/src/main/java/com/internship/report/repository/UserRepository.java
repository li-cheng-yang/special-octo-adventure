package com.internship.report.repository;

import com.internship.report.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByRole(User.Role role);

    Page<User> findByRole(User.Role role, Pageable pageable);

    List<User> findByRoleAndStatus(User.Role role, User.Status status);

    @Query("SELECT u FROM User u WHERE u.role = :role AND " +
           "(u.realName LIKE %:keyword% OR u.studentNo LIKE %:keyword% OR u.username LIKE %:keyword%)")
    Page<User> searchByRoleAndKeyword(@Param("role") User.Role role,
                                       @Param("keyword") String keyword,
                                       Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.Role role);

    @Query("SELECT DISTINCT i.student FROM Internship i WHERE i.teacher.id = :teacherId")
    List<User> findStudentsByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT DISTINCT i.student FROM Internship i WHERE i.teacher.id = :teacherId AND i.student.department = :department")
    List<User> findStudentsByTeacherIdAndDepartment(@Param("teacherId") Long teacherId,
                                                     @Param("department") String department);

    @Query("SELECT DISTINCT i.student FROM Internship i WHERE i.teacher.id = :teacherId AND " +
           "(:studentNo IS NULL OR i.student.studentNo LIKE %:studentNo%) AND " +
           "(:realName IS NULL OR i.student.realName LIKE %:realName%) AND " +
           "(:major IS NULL OR i.student.major LIKE %:major%) AND " +
           "(:className IS NULL OR i.student.className LIKE %:className%)")
    List<User> searchStudentsByTeacherId(@Param("teacherId") Long teacherId,
                                          @Param("studentNo") String studentNo,
                                          @Param("realName") String realName,
                                          @Param("major") String major,
                                          @Param("className") String className);

    @Query("SELECT DISTINCT i.student FROM Internship i WHERE i.teacher.id = :teacherId AND " +
           "i.student.department = :department AND " +
           "(:studentNo IS NULL OR i.student.studentNo LIKE %:studentNo%) AND " +
           "(:realName IS NULL OR i.student.realName LIKE %:realName%) AND " +
           "(:major IS NULL OR i.student.major LIKE %:major%) AND " +
           "(:className IS NULL OR i.student.className LIKE %:className%)")
    List<User> searchStudentsByTeacherIdAndDepartment(@Param("teacherId") Long teacherId,
                                                       @Param("department") String department,
                                                       @Param("studentNo") String studentNo,
                                                       @Param("realName") String realName,
                                                       @Param("major") String major,
                                                       @Param("className") String className);
}
