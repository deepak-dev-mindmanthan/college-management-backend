package org.collegemanagement.repositories;

import org.collegemanagement.entity.discipline.DisciplinaryCase;
import org.collegemanagement.enums.DisciplinaryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DisciplinaryCaseRepository extends JpaRepository<DisciplinaryCase, Long> {

    /**
     * Find disciplinary case by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT dc FROM DisciplinaryCase dc
            WHERE dc.uuid = :uuid
            AND dc.college.id = :collegeId
            """)
    Optional<DisciplinaryCase> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find all disciplinary cases by college ID with pagination
     */
    @Query("""
            SELECT dc FROM DisciplinaryCase dc
            WHERE dc.college.id = :collegeId
            ORDER BY dc.incidentDate DESC, dc.createdAt DESC
            """)
    Page<DisciplinaryCase> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find disciplinary cases by student UUID and college ID
     */
    @Query("""
            SELECT dc FROM DisciplinaryCase dc
            WHERE dc.student.uuid = :studentUuid
            AND dc.college.id = :collegeId
            ORDER BY dc.incidentDate DESC
            """)
    Page<DisciplinaryCase> findByStudentUuidAndCollegeId(
            @Param("studentUuid") String studentUuid,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Find disciplinary cases by status and college ID
     */
    @Query("""
            SELECT dc FROM DisciplinaryCase dc
            WHERE dc.status = :status
            AND dc.college.id = :collegeId
            ORDER BY dc.incidentDate DESC
            """)
    Page<DisciplinaryCase> findByStatusAndCollegeId(
            @Param("status") DisciplinaryStatus status,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Find disciplinary cases by student UUID, status, and college ID
     */
    @Query("""
            SELECT dc FROM DisciplinaryCase dc
            WHERE dc.student.uuid = :studentUuid
            AND dc.status = :status
            AND dc.college.id = :collegeId
            ORDER BY dc.incidentDate DESC
            """)
    Page<DisciplinaryCase> findByStudentUuidAndStatusAndCollegeId(
            @Param("studentUuid") String studentUuid,
            @Param("status") DisciplinaryStatus status,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Find disciplinary cases by date range and college ID
     */
    @Query("""
            SELECT dc FROM DisciplinaryCase dc
            WHERE dc.college.id = :collegeId
            AND dc.incidentDate BETWEEN :startDate AND :endDate
            ORDER BY dc.incidentDate DESC
            """)
    Page<DisciplinaryCase> findByDateRangeAndCollegeId(
            @Param("collegeId") Long collegeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    /**
     * Count disciplinary cases by status and college ID
     */
    @Query("""
            SELECT COUNT(dc) FROM DisciplinaryCase dc
            WHERE dc.status = :status
            AND dc.college.id = :collegeId
            """)
    long countByStatusAndCollegeId(@Param("status") DisciplinaryStatus status, @Param("collegeId") Long collegeId);

    /**
     * Count disciplinary cases by student UUID and college ID
     */
    @Query("""
            SELECT COUNT(dc) FROM DisciplinaryCase dc
            WHERE dc.student.uuid = :studentUuid
            AND dc.college.id = :collegeId
            """)
    long countByStudentUuidAndCollegeId(@Param("studentUuid") String studentUuid, @Param("collegeId") Long collegeId);
}

