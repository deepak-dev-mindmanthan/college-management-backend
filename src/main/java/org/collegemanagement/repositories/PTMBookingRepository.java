package org.collegemanagement.repositories;

import org.collegemanagement.entity.ptm.PTMBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PTMBookingRepository extends JpaRepository<PTMBooking, Long> {

    /**
     * Find PTM booking by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT pb FROM PTMBooking pb
            WHERE pb.uuid = :uuid
            AND pb.student.college.id = :collegeId
            """)
    Optional<PTMBooking> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find all PTM bookings by college ID with pagination
     */
    @Query("""
            SELECT pb FROM PTMBooking pb
            WHERE pb.student.college.id = :collegeId
            ORDER BY pb.bookedAt DESC
            """)
    Page<PTMBooking> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find PTM bookings by parent UUID and college ID
     */
    @Query("""
            SELECT pb FROM PTMBooking pb
            WHERE pb.parent.uuid = :parentUuid
            AND pb.student.college.id = :collegeId
            ORDER BY pb.bookedAt DESC
            """)
    Page<PTMBooking> findByParentUuidAndCollegeId(
            @Param("parentUuid") String parentUuid,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Find PTM bookings by student UUID and college ID
     */
    @Query("""
            SELECT pb FROM PTMBooking pb
            WHERE pb.student.uuid = :studentUuid
            AND pb.student.college.id = :collegeId
            ORDER BY pb.bookedAt DESC
            """)
    Page<PTMBooking> findByStudentUuidAndCollegeId(
            @Param("studentUuid") String studentUuid,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Find PTM booking by slot ID (for checking if slot is already booked)
     */
    @Query("""
            SELECT pb FROM PTMBooking pb
            WHERE pb.slot.id = :slotId
            """)
    Optional<PTMBooking> findBySlotId(@Param("slotId") Long slotId);

    /**
     * Check if slot is already booked
     */
    @Query("""
            SELECT COUNT(pb) > 0 FROM PTMBooking pb
            WHERE pb.slot.id = :slotId
            """)
    boolean existsBySlotId(@Param("slotId") Long slotId);

    /**
     * Find PTM bookings by teacher UUID and college ID
     */
    @Query("""
            SELECT pb FROM PTMBooking pb
            WHERE pb.slot.parentUser.uuid = :teacherUuid
            AND pb.student.college.id = :collegeId
            ORDER BY pb.bookedAt DESC
            """)
    Page<PTMBooking> findByTeacherUuidAndCollegeId(
            @Param("teacherUuid") String teacherUuid,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Find PTM bookings by date and college ID
     */
    @Query("""
            SELECT pb FROM PTMBooking pb
            WHERE pb.slot.date = :date
            AND pb.student.college.id = :collegeId
            ORDER BY pb.slot.startTime ASC
            """)
    Page<PTMBooking> findByDateAndCollegeId(
            @Param("date") LocalDate date,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );
}

