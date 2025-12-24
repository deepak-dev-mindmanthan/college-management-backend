package org.collegemanagement.repositories;

import org.collegemanagement.entity.academic.Subject;
import org.collegemanagement.entity.tenant.College;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CollegeRepository extends JpaRepository<College, Long> {
    boolean existsByName(String collegeName);
    College findCollegeByName(String name);
    College findCollegeByEmail(String email);
    boolean existsCollegeByPhone(String mobile);
    College findCollegeByPhone(String mobile);
    Optional<College> findByUuid(String uuid);
    boolean existsByUuid(String uuid);
    boolean existsCollegeByEmail(String email);

    boolean existsCollegeByShortCode(String shortCode);

    @Query("""
    SELECT s FROM Subject s
    JOIN s.classRoom c
    WHERE c.college.id = :collegeId
    """)
    List<Subject> findSubjectsByCollegeId(Long collegeId);

    @EntityGraph(attributePaths = {
            "departments",
            "departments.head",
            "academicYears"
    })
    Optional<College> findByUuidAndId(String uuid, Long collegeId);


}
