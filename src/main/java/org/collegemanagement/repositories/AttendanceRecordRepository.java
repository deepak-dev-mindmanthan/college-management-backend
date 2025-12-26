package org.collegemanagement.repositories;

import org.collegemanagement.entity.attendance.AttendanceRecord;
import org.collegemanagement.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    /**
     * Find attendance record by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT r FROM AttendanceRecord r
            JOIN r.attendanceSession s
            WHERE r.uuid = :uuid
            AND s.college.id = :collegeId
            """)
    Optional<AttendanceRecord> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find attendance record by session and student
     */
    @Query("""
            SELECT r FROM AttendanceRecord r
            WHERE r.attendanceSession.id = :sessionId
            AND r.student.id = :studentId
            """)
    Optional<AttendanceRecord> findBySessionIdAndStudentId(@Param("sessionId") Long sessionId, @Param("studentId") Long studentId);

    /**
     * Find all attendance records by session (college isolation)
     */
    @Query("""
            SELECT r FROM AttendanceRecord r
            JOIN r.attendanceSession s
            WHERE s.id = :sessionId
            AND s.college.id = :collegeId
            ORDER BY r.student.user.name ASC
            """)
    List<AttendanceRecord> findBySessionIdAndCollegeId(@Param("sessionId") Long sessionId, @Param("collegeId") Long collegeId);

    /**
     * Find all attendance records by student and date range (college isolation)
     */
    @Query("""
            SELECT r FROM AttendanceRecord r
            JOIN r.attendanceSession s
            WHERE r.student.id = :studentId
            AND s.date BETWEEN :startDate AND :endDate
            AND s.college.id = :collegeId
            ORDER BY s.date DESC, s.sessionType ASC
            """)
    List<AttendanceRecord> findByStudentIdAndDateRangeAndCollegeId(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("collegeId") Long collegeId
    );

    /**
     * Find all attendance records by student UUID and date range (college isolation)
     */
    @Query("""
            SELECT r FROM AttendanceRecord r
            JOIN r.attendanceSession s
            WHERE r.student.uuid = :studentUuid
            AND s.date BETWEEN :startDate AND :endDate
            AND s.college.id = :collegeId
            ORDER BY s.date DESC, s.sessionType ASC
            """)
    List<AttendanceRecord> findByStudentUuidAndDateRangeAndCollegeId(
            @Param("studentUuid") String studentUuid,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("collegeId") Long collegeId
    );

    /**
     * Find all attendance records by student (college isolation)
     */
    @Query("""
            SELECT r FROM AttendanceRecord r
            JOIN r.attendanceSession s
            WHERE r.student.id = :studentId
            AND s.college.id = :collegeId
            ORDER BY s.date DESC, s.sessionType ASC
            """)
    Page<AttendanceRecord> findByStudentIdAndCollegeId(@Param("studentId") Long studentId, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find all attendance records by student UUID (college isolation)
     */
    @Query("""
            SELECT r FROM AttendanceRecord r
            JOIN r.attendanceSession s
            WHERE r.student.uuid = :studentUuid
            AND s.college.id = :collegeId
            ORDER BY s.date DESC, s.sessionType ASC
            """)
    Page<AttendanceRecord> findByStudentUuidAndCollegeId(@Param("studentUuid") String studentUuid, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Count attendance records by status and student in date range (college isolation)
     */
    @Query("""
            SELECT COUNT(r) FROM AttendanceRecord r
            JOIN r.attendanceSession s
            WHERE r.student.id = :studentId
            AND r.status = :status
            AND s.date BETWEEN :startDate AND :endDate
            AND s.college.id = :collegeId
            """)
    Long countByStudentIdAndStatusAndDateRangeAndCollegeId(
            @Param("studentId") Long studentId,
            @Param("status") AttendanceStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("collegeId") Long collegeId
    );

    /**
     * Count total attendance records by student in date range (college isolation)
     */
    @Query("""
            SELECT COUNT(r) FROM AttendanceRecord r
            JOIN r.attendanceSession s
            WHERE r.student.id = :studentId
            AND s.date BETWEEN :startDate AND :endDate
            AND s.college.id = :collegeId
            """)
    Long countByStudentIdAndDateRangeAndCollegeId(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("collegeId") Long collegeId
    );

    /**
     * Count attendance records by status and class in date range (college isolation)
     */
    @Query("""
            SELECT COUNT(r) FROM AttendanceRecord r
            JOIN r.attendanceSession s
            WHERE s.classRoom.id = :classId
            AND r.status = :status
            AND s.date BETWEEN :startDate AND :endDate
            AND s.college.id = :collegeId
            """)
    Long countByClassIdAndStatusAndDateRangeAndCollegeId(
            @Param("classId") Long classId,
            @Param("status") AttendanceStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("collegeId") Long collegeId
    );

    /**
     * Count total attendance records by class in date range (college isolation)
     */
    @Query("""
            SELECT COUNT(r) FROM AttendanceRecord r
            JOIN r.attendanceSession s
            WHERE s.classRoom.id = :classId
            AND s.date BETWEEN :startDate AND :endDate
            AND s.college.id = :collegeId
            """)
    Long countByClassIdAndDateRangeAndCollegeId(
            @Param("classId") Long classId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("collegeId") Long collegeId
    );

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

