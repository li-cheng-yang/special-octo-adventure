package com.internship.report.repository;

import com.internship.report.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);

    boolean existsByName(String name);

    List<Company> findByStatus(Company.Status status);

    @Query("SELECT c FROM Company c WHERE " +
           "(:keyword IS NULL OR c.name LIKE %:keyword% OR c.industry LIKE %:keyword% OR c.city LIKE %:keyword%) AND " +
           "(:status IS NULL OR c.status = :status)")
    Page<Company> search(@Param("keyword") String keyword, 
                         @Param("status") Company.Status status, 
                         Pageable pageable);

    @Query("SELECT COUNT(c) FROM Company c WHERE c.status = 'ACTIVE'")
    long countActive();
}
