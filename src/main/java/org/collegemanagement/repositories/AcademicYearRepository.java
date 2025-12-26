package org.collegemanagement.repositories;

import org.collegemanagement.entity.tenant.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {

    /**
     * Find academic year by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT ay FROM AcademicYear ay
            WHERE ay.uuid = :uuid
            AND ay.college.id = :collegeId
            """)
    Optional<AcademicYear> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find active academic year by college ID
     */
    @Query("""
            SELECT ay FROM AcademicYear ay
            WHERE ay.college.id = :collegeId
            AND ay.active = true
            ORDER BY ay.startDate DESC
            """)
    Optional<AcademicYear> findActiveByCollegeId(@Param("collegeId") Long collegeId);
}

