package org.collegemanagement.repositories;

import org.collegemanagement.entity.fees.FeeReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FeeReceiptRepository extends JpaRepository<FeeReceipt, Long> {

    @Query("""
            SELECT fr FROM FeeReceipt fr
            WHERE fr.receiptNumber = :receiptNumber
            """)
    Optional<FeeReceipt> findByReceiptNumber(@Param("receiptNumber") String receiptNumber);
}
