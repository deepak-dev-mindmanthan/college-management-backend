package org.collegemanagement.repositories;

import org.collegemanagement.entity.ptm.PTMSlot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PTMSlotRepository extends JpaRepository<PTMSlot, Long> {

    /**
     * Find PTM slot by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT ps FROM PTMSlot ps
            WHERE ps.uuid = :uuid
            AND ps.college.id = :collegeId
            """)
    Optional<PTMSlot> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find all PTM slots by college ID with pagination
     */
    @Query("""
            SELECT ps FROM PTMSlot ps
            WHERE ps.college.id = :collegeId
            ORDER BY ps.date ASC, ps.startTime ASC
            """)
    Page<PTMSlot> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find active PTM slots by college ID
     */
    @Query("""
            SELECT ps FROM PTMSlot ps
            WHERE ps.college.id = :collegeId
            AND ps.active = true
            ORDER BY ps.date ASC, ps.startTime ASC
            """)
    Page<PTMSlot> findActiveByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find PTM slots by teacher UUID and college ID
     */
    @Query("""
            SELECT ps FROM PTMSlot ps
            WHERE ps.parentUser.uuid = :teacherUuid
            AND ps.college.id = :collegeId
            ORDER BY ps.date ASC, ps.startTime ASC
            """)
    Page<PTMSlot> findByTeacherUuidAndCollegeId(
            @Param("teacherUuid") String teacherUuid,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Find active PTM slots by teacher UUID and college ID
     */
    @Query("""
            SELECT ps FROM PTMSlot ps
            WHERE ps.parentUser.uuid = :teacherUuid
            AND ps.college.id = :collegeId
            AND ps.active = true
            ORDER BY ps.date ASC, ps.startTime ASC
            """)
    Page<PTMSlot> findActiveByTeacherUuidAndCollegeId(
            @Param("teacherUuid") String teacherUuid,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Find PTM slots by date and college ID
     */
    @Query("""
            SELECT ps FROM PTMSlot ps
            WHERE ps.date = :date
            AND ps.college.id = :collegeId
            ORDER BY ps.startTime ASC
            """)
    List<PTMSlot> findByDateAndCollegeId(@Param("date") LocalDate date, @Param("collegeId") Long collegeId);

    /**
     * Find active PTM slots by date and college ID
     */
    @Query("""
            SELECT ps FROM PTMSlot ps
            WHERE ps.date = :date
            AND ps.college.id = :collegeId
            AND ps.active = true
            ORDER BY ps.startTime ASC
            """)
    List<PTMSlot> findActiveByDateAndCollegeId(@Param("date") LocalDate date, @Param("collegeId") Long collegeId);

    /**
     * Check if slot exists by slot ID (for booking validation)
     */
    boolean existsById(Long slotId);
}

