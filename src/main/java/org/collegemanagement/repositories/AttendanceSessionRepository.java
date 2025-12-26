package org.collegemanagement.repositories;

import org.collegemanagement.entity.attendance.AttendanceSession;
import org.collegemanagement.enums.AttendanceSessionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    /**
     * Find attendance session by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT s FROM AttendanceSession s
            WHERE s.uuid = :uuid
            AND s.college.id = :collegeId
            """)
    Optional<AttendanceSession> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find attendance session by class, date, and session type (college isolation)
     */
    @Query("""
            SELECT s FROM AttendanceSession s
            WHERE s.classRoom.id = :classId
            AND s.date = :date
            AND s.sessionType = :sessionType
            AND s.college.id = :collegeId
            """)
    Optional<AttendanceSession> findByClassIdAndDateAndSessionTypeAndCollegeId(
            @Param("classId") Long classId,
            @Param("date") LocalDate date,
            @Param("sessionType") AttendanceSessionType sessionType,
            @Param("collegeId") Long collegeId
    );

    /**
     * Find all attendance sessions by class and date range (college isolation)
     */
    @Query("""
            SELECT s FROM AttendanceSession s
            WHERE s.classRoom.id = :classId
            AND s.date BETWEEN :startDate AND :endDate
            AND s.college.id = :collegeId
            ORDER BY s.date DESC, s.sessionType ASC
            """)
    List<AttendanceSession> findByClassIdAndDateRangeAndCollegeId(
            @Param("classId") Long classId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("collegeId") Long collegeId
    );

    /**
     * Find all attendance sessions by class UUID and date range (college isolation)
     */
    @Query("""
            SELECT s FROM AttendanceSession s
            WHERE s.classRoom.uuid = :classUuid
            AND s.date BETWEEN :startDate AND :endDate
            AND s.college.id = :collegeId
            ORDER BY s.date DESC, s.sessionType ASC
            """)
    List<AttendanceSession> findByClassUuidAndDateRangeAndCollegeId(
            @Param("classUuid") String classUuid,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("collegeId") Long collegeId
    );

    /**
     * Find all attendance sessions by class (college isolation)
     */
    @Query("""
            SELECT s FROM AttendanceSession s
            WHERE s.classRoom.id = :classId
            AND s.college.id = :collegeId
            ORDER BY s.date DESC, s.sessionType ASC
            """)
    Page<AttendanceSession> findByClassIdAndCollegeId(@Param("classId") Long classId, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find all attendance sessions by class UUID (college isolation)
     */
    @Query("""
            SELECT s FROM AttendanceSession s
            WHERE s.classRoom.uuid = :classUuid
            AND s.college.id = :collegeId
            ORDER BY s.date DESC, s.sessionType ASC
            """)
    Page<AttendanceSession> findByClassUuidAndCollegeId(@Param("classUuid") String classUuid, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find all attendance sessions by date range (college isolation)
     */
    @Query("""
            SELECT s FROM AttendanceSession s
            WHERE s.date BETWEEN :startDate AND :endDate
            AND s.college.id = :collegeId
            ORDER BY s.date DESC, s.classRoom.name ASC, s.sessionType ASC
            """)
    Page<AttendanceSession> findByDateRangeAndCollegeId(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Find all attendance sessions by date (college isolation)
     */
    @Query("""
            SELECT s FROM AttendanceSession s
            WHERE s.date = :date
            AND s.college.id = :collegeId
            ORDER BY s.classRoom.name ASC, s.sessionType ASC
            """)
    List<AttendanceSession> findByDateAndCollegeId(@Param("date") LocalDate date, @Param("collegeId") Long collegeId);

    /**
     * Count distinct sessions by class in date range (college isolation)
     */
    @Query("""
            SELECT COUNT(DISTINCT s.id) FROM AttendanceSession s
            WHERE s.classRoom.id = :classId
            AND s.date BETWEEN :startDate AND :endDate
            AND s.college.id = :collegeId
            """)
    Long countDistinctSessionsByClassIdAndDateRangeAndCollegeId(
            @Param("classId") Long classId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("collegeId") Long collegeId
    );
}

