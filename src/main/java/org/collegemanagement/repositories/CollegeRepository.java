package org.collegemanagement.repositories;

import org.collegemanagement.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollegeRepository extends JpaRepository<College, Long> {
    boolean existsByName(String collegeName);
    College findCollegeByName(String name);
    College findCollegeByEmail(String email);
    boolean existsCollegeByPhone(String mobile);
    College findCollegeByPhone(String mobile);
}
