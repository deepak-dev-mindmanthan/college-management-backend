package org.collegemanagement.repositories;

import org.collegemanagement.entity.staff.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StaffProfileRepository extends JpaRepository<StaffProfile, Long> {

    Optional<StaffProfile> findByUserId(Long userId);

    @Query("""
            SELECT sp FROM StaffProfile sp
            WHERE sp.user.id = :userId
            AND sp.college.id = :collegeId
            """)
    Optional<StaffProfile> findByUserIdAndCollegeId(@Param("userId") Long userId, @Param("collegeId") Long collegeId);

    @Query("""
            SELECT sp FROM StaffProfile sp
            WHERE sp.user.uuid = :userUuid
            AND sp.college.id = :collegeId
            """)
    Optional<StaffProfile> findByUserUuidAndCollegeId(@Param("userUuid") String userUuid, @Param("collegeId") Long collegeId);
}

