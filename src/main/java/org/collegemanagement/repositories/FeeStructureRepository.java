package org.collegemanagement.repositories;

import org.collegemanagement.entity.fees.FeeStructure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {

    /**
     * Find fee structure by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT fs FROM FeeStructure fs
            WHERE fs.uuid = :uuid
            AND fs.college.id = :collegeId
            """)
    Optional<FeeStructure> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find fee structure by class ID and college ID
     */
    @Query("""
            SELECT fs FROM FeeStructure fs
            WHERE fs.classRoom.id = :classId
            AND fs.college.id = :collegeId
            """)
    Optional<FeeStructure> findByClassIdAndCollegeId(@Param("classId") Long classId, @Param("collegeId") Long collegeId);

    /**
     * Find fee structure by class UUID and college ID
     */
    @Query("""
            SELECT fs FROM FeeStructure fs
            WHERE fs.classRoom.uuid = :classUuid
            AND fs.college.id = :collegeId
            """)
    Optional<FeeStructure> findByClassUuidAndCollegeId(@Param("classUuid") String classUuid, @Param("collegeId") Long collegeId);

    /**
     * Find all fee structures by college ID with pagination
     */
    @Query("""
            SELECT fs FROM FeeStructure fs
            WHERE fs.college.id = :collegeId
            ORDER BY fs.classRoom.name ASC, fs.classRoom.section ASC
            """)
    Page<FeeStructure> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find all fee structures by class UUID and college ID
     */
    @Query("""
            SELECT fs FROM FeeStructure fs
            WHERE fs.classRoom.uuid = :classUuid
            AND fs.college.id = :collegeId
            """)
    List<FeeStructure> findAllByClassUuidAndCollegeId(@Param("classUuid") String classUuid, @Param("collegeId") Long collegeId);

    /**
     * Check if fee structure exists for class and college
     */
    @Query("""
            SELECT COUNT(fs) > 0 FROM FeeStructure fs
            WHERE fs.classRoom.id = :classId
            AND fs.college.id = :collegeId
            """)
    boolean existsByClassIdAndCollegeId(@Param("classId") Long classId, @Param("collegeId") Long collegeId);
}

