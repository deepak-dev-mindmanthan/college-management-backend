package org.collegemanagement.repositories;

import org.collegemanagement.entity.hostel.Hostel;
import org.collegemanagement.enums.HostelType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HostelRepository extends JpaRepository<Hostel, Long> {

    /**
     * Find hostel by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT h FROM Hostel h
            WHERE h.uuid = :uuid
            AND h.college.id = :collegeId
            """)
    Optional<Hostel> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find all hostels by college ID with pagination
     */
    @Query("""
            SELECT h FROM Hostel h
            WHERE h.college.id = :collegeId
            ORDER BY h.name ASC
            """)
    Page<Hostel> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find all hostels by type and college ID with pagination
     */
    @Query("""
            SELECT h FROM Hostel h
            WHERE h.type = :type
            AND h.college.id = :collegeId
            ORDER BY h.name ASC
            """)
    Page<Hostel> findByTypeAndCollegeId(@Param("type") HostelType type, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Check if hostel name exists for a college
     */
    @Query("""
            SELECT COUNT(h) > 0 FROM Hostel h
            WHERE h.name = :name
            AND h.college.id = :collegeId
            """)
    boolean existsByNameAndCollegeId(@Param("name") String name, @Param("collegeId") Long collegeId);

    /**
     * Check if hostel name exists for a college excluding a specific hostel (for updates)
     */
    @Query("""
            SELECT COUNT(h) > 0 FROM Hostel h
            WHERE h.name = :name
            AND h.college.id = :collegeId
            AND h.id != :excludeId
            """)
    boolean existsByNameAndCollegeIdAndIdNot(@Param("name") String name, @Param("collegeId") Long collegeId, @Param("excludeId") Long excludeId);

    /**
     * Search hostels by name within a college
     */
    @Query("""
            SELECT h FROM Hostel h
            WHERE h.college.id = :collegeId
            AND LOWER(h.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            ORDER BY h.name ASC
            """)
    Page<Hostel> searchByCollegeId(@Param("collegeId") Long collegeId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Count hostels by college ID
     */
    @Query("""
            SELECT COUNT(h) FROM Hostel h
            WHERE h.college.id = :collegeId
            """)
    long countByCollegeId(@Param("collegeId") Long collegeId);
}

