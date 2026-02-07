package org.collegemanagement.repositories;

import org.collegemanagement.entity.fees.FeeInstallmentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeeInstallmentTemplateRepository extends JpaRepository<FeeInstallmentTemplate, Long> {

    @Query("""
            SELECT fit FROM FeeInstallmentTemplate fit
            WHERE fit.feeStructure.uuid = :feeStructureUuid
            AND fit.feeStructure.college.id = :collegeId
            ORDER BY fit.dueDate ASC
            """)
    List<FeeInstallmentTemplate> findByFeeStructureUuidAndCollegeId(
            @Param("feeStructureUuid") String feeStructureUuid,
            @Param("collegeId") Long collegeId
    );
}
