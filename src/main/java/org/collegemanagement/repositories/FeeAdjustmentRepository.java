package org.collegemanagement.repositories;

import org.collegemanagement.entity.fees.FeeAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeeAdjustmentRepository extends JpaRepository<FeeAdjustment, Long> {

    @Query("""
            SELECT fa FROM FeeAdjustment fa
            WHERE fa.studentFee.uuid = :studentFeeUuid
            AND fa.studentFee.student.college.id = :collegeId
            ORDER BY fa.createdAt DESC
            """)
    List<FeeAdjustment> findByStudentFeeUuidAndCollegeId(@Param("studentFeeUuid") String studentFeeUuid,
                                                         @Param("collegeId") Long collegeId);
}
