package org.collegemanagement.repositories;

import org.collegemanagement.entity.academic.ClassRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {

    Optional<ClassRoom> findByUuid(String uuid);

    @Query("""
            SELECT c FROM ClassRoom c
            WHERE c.uuid = :uuid
            AND c.college.id = :collegeId
            """)
    Optional<ClassRoom> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);
}

