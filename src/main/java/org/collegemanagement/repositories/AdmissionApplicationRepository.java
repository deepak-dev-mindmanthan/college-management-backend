package org.collegemanagement.repositories;

import org.collegemanagement.entity.admission.AdmissionApplication;
import org.collegemanagement.enums.AdmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdmissionApplicationRepository extends JpaRepository<AdmissionApplication, Long> {

    /**
     * Find admission application by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT a FROM AdmissionApplication a
            WHERE a.uuid = :uuid
            AND a.college.id = :collegeId
            """)
    Optional<AdmissionApplication> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find admission application by application number and college ID
     */
    @Query("""
            SELECT a FROM AdmissionApplication a
            WHERE a.applicationNo = :applicationNo
            AND a.college.id = :collegeId
            """)
    Optional<AdmissionApplication> findByApplicationNoAndCollegeId(@Param("applicationNo") String applicationNo, @Param("collegeId") Long collegeId);

    /**
     * Find all admission applications by college ID with pagination
     */
    @Query("""
            SELECT a FROM AdmissionApplication a
            WHERE a.college.id = :collegeId
            ORDER BY a.submittedAt DESC, a.createdAt DESC
            """)
    Page<AdmissionApplication> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find admission applications by status and college ID
     */
    @Query("""
            SELECT a FROM AdmissionApplication a
            WHERE a.status = :status
            AND a.college.id = :collegeId
            ORDER BY a.submittedAt DESC, a.createdAt DESC
            """)
    Page<AdmissionApplication> findByStatusAndCollegeId(@Param("status") AdmissionStatus status, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find admission applications by class UUID and college ID
     */
    @Query("""
            SELECT a FROM AdmissionApplication a
            WHERE a.appliedClass.uuid = :classUuid
            AND a.college.id = :collegeId
            ORDER BY a.submittedAt DESC, a.createdAt DESC
            """)
    Page<AdmissionApplication> findByClassUuidAndCollegeId(@Param("classUuid") String classUuid, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Search admission applications by student name, email, phone, or application number
     */
    @Query("""
            SELECT a FROM AdmissionApplication a
            WHERE a.college.id = :collegeId
            AND (LOWER(a.studentName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(a.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(a.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(a.applicationNo) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
            ORDER BY a.submittedAt DESC, a.createdAt DESC
            """)
    Page<AdmissionApplication> searchApplicationsByCollegeId(@Param("collegeId") Long collegeId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Check if application number exists within a college
     */
    @Query("""
            SELECT COUNT(a) > 0 FROM AdmissionApplication a
            WHERE a.applicationNo = :applicationNo
            AND a.college.id = :collegeId
            """)
    boolean existsByApplicationNoAndCollegeId(@Param("applicationNo") String applicationNo, @Param("collegeId") Long collegeId);

    /**
     * Count admission applications by status and college ID
     */
    @Query("""
            SELECT COUNT(a) FROM AdmissionApplication a
            WHERE a.status = :status
            AND a.college.id = :collegeId
            """)
    long countByStatusAndCollegeId(@Param("status") AdmissionStatus status, @Param("collegeId") Long collegeId);

    /**
     * Count total admission applications by college ID
     */
    @Query("""
            SELECT COUNT(a) FROM AdmissionApplication a
            WHERE a.college.id = :collegeId
            """)
    long countByCollegeId(@Param("collegeId") Long collegeId);
}

