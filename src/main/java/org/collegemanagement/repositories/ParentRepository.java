package org.collegemanagement.repositories;

import org.collegemanagement.entity.student.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {

    /**
     * Find parent by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT p FROM Parent p
            WHERE p.uuid = :uuid
            AND p.college.id = :collegeId
            """)
    Optional<Parent> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find parent by user ID and college ID
     */
    @Query("""
            SELECT p FROM Parent p
            WHERE p.user.id = :userId
            AND p.college.id = :collegeId
            """)
    Optional<Parent> findByUserIdAndCollegeId(@Param("userId") Long userId, @Param("collegeId") Long collegeId);
}

