package org.collegemanagement.repositories;

import org.collegemanagement.entity.fees.FeeInstallment;
import org.collegemanagement.enums.InstallmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FeeInstallmentRepository extends JpaRepository<FeeInstallment, Long> {

    @Query("""
            SELECT fi FROM FeeInstallment fi
            WHERE fi.studentFee.uuid = :studentFeeUuid
            AND fi.studentFee.student.college.id = :collegeId
            ORDER BY fi.dueDate ASC
            """)
    List<FeeInstallment> findByStudentFeeUuidAndCollegeId(@Param("studentFeeUuid") String studentFeeUuid,
                                                          @Param("collegeId") Long collegeId);

    @Query("""
            SELECT fi FROM FeeInstallment fi
            WHERE fi.studentFee.uuid = :studentFeeUuid
            AND fi.studentFee.student.college.id = :collegeId
            ORDER BY fi.dueDate ASC
            """)
    Page<FeeInstallment> findByStudentFeeUuidAndCollegeId(@Param("studentFeeUuid") String studentFeeUuid,
                                                          @Param("collegeId") Long collegeId,
                                                          Pageable pageable);

    @Query("""
            SELECT fi FROM FeeInstallment fi
            WHERE fi.studentFee.id = :studentFeeId
            ORDER BY fi.dueDate ASC
            """)
    List<FeeInstallment> findByStudentFeeId(@Param("studentFeeId") Long studentFeeId);

    @Modifying
    @Query("""
            UPDATE FeeInstallment fi
            SET fi.status = :status
            WHERE fi.dueDate < :today
            AND fi.dueAmount > 0
            AND fi.status IN ('PENDING', 'PARTIALLY_PAID')
            """)
    int markOverdue(@Param("status") InstallmentStatus status, @Param("today") LocalDate today);
}
