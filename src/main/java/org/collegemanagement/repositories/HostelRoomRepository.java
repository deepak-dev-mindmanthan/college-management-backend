package org.collegemanagement.repositories;

import org.collegemanagement.entity.hostel.HostelRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HostelRoomRepository extends JpaRepository<HostelRoom, Long> {

    /**
     * Find hostel room by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT hr FROM HostelRoom hr
            WHERE hr.uuid = :uuid
            AND hr.hostel.college.id = :collegeId
            """)
    Optional<HostelRoom> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find all hostel rooms by college ID with pagination
     */
    @Query("""
            SELECT hr FROM HostelRoom hr
            WHERE hr.hostel.college.id = :collegeId
            ORDER BY hr.hostel.name ASC, hr.roomNumber ASC
            """)
    Page<HostelRoom> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find all hostel rooms by hostel UUID and college ID with pagination
     */
    @Query("""
            SELECT hr FROM HostelRoom hr
            WHERE hr.hostel.uuid = :hostelUuid
            AND hr.hostel.college.id = :collegeId
            ORDER BY hr.roomNumber ASC
            """)
    Page<HostelRoom> findByHostelUuidAndCollegeId(@Param("hostelUuid") String hostelUuid, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find all hostel rooms by hostel UUID and college ID (without pagination)
     */
    @Query("""
            SELECT hr FROM HostelRoom hr
            WHERE hr.hostel.uuid = :hostelUuid
            AND hr.hostel.college.id = :collegeId
            ORDER BY hr.roomNumber ASC
            """)
    List<HostelRoom> findAllByHostelUuidAndCollegeId(@Param("hostelUuid") String hostelUuid, @Param("collegeId") Long collegeId);

    /**
     * Check if room number exists for a hostel
     */
    @Query("""
            SELECT COUNT(hr) > 0 FROM HostelRoom hr
            WHERE hr.hostel.uuid = :hostelUuid
            AND hr.roomNumber = :roomNumber
            AND hr.hostel.college.id = :collegeId
            """)
    boolean existsByHostelUuidAndRoomNumberAndCollegeId(@Param("hostelUuid") String hostelUuid, @Param("roomNumber") String roomNumber, @Param("collegeId") Long collegeId);

    /**
     * Check if room number exists for a hostel excluding a specific room (for updates)
     */
    @Query("""
            SELECT COUNT(hr) > 0 FROM HostelRoom hr
            WHERE hr.hostel.uuid = :hostelUuid
            AND hr.roomNumber = :roomNumber
            AND hr.hostel.college.id = :collegeId
            AND hr.id != :excludeId
            """)
    boolean existsByHostelUuidAndRoomNumberAndCollegeIdAndIdNot(@Param("hostelUuid") String hostelUuid, @Param("roomNumber") String roomNumber, @Param("collegeId") Long collegeId, @Param("excludeId") Long excludeId);

    /**
     * Search hostel rooms by room number or hostel name within a college
     */
    @Query("""
            SELECT hr FROM HostelRoom hr
            WHERE hr.hostel.college.id = :collegeId
            AND (LOWER(hr.roomNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(hr.hostel.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
            ORDER BY hr.hostel.name ASC, hr.roomNumber ASC
            """)
    Page<HostelRoom> searchByCollegeId(@Param("collegeId") Long collegeId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Count hostel rooms by college ID
     */
    @Query("""
            SELECT COUNT(hr) FROM HostelRoom hr
            WHERE hr.hostel.college.id = :collegeId
            """)
    long countByCollegeId(@Param("collegeId") Long collegeId);

    /**
     * Count hostel rooms by hostel UUID and college ID
     */
    @Query("""
            SELECT COUNT(hr) FROM HostelRoom hr
            WHERE hr.hostel.uuid = :hostelUuid
            AND hr.hostel.college.id = :collegeId
            """)
    long countByHostelUuidAndCollegeId(@Param("hostelUuid") String hostelUuid, @Param("collegeId") Long collegeId);
}

