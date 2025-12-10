package org.collegemanagement.repositories;
import org.collegemanagement.entity.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeeRepository extends JpaRepository<Fee, Long> {
    List<Fee> findByStudentId(Long studentId);
    List<Fee> findByStudentCollegeId(Long collegeId);
    List<Fee> findByStudentCollegeIdAndStatus(Long collegeId, org.collegemanagement.enums.FeeStatus status);
    @Query("SELECT SUM(f.amount) FROM Fee f WHERE f.student.id = :studentId AND f.status = 'PENDING'")
    Double getPendingFees(@Param("studentId") Long studentId);

}
