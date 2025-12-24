package org.collegemanagement.repositories;

import org.collegemanagement.entity.academic.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findByUuid(String uuid);

    @Query("""
            SELECT s FROM Subject s
            JOIN s.classRoom c
            WHERE s.uuid = :uuid
            AND c.college.id = :collegeId
            """)
    Optional<Subject> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);
}

