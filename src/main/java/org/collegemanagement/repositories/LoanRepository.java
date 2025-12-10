package org.collegemanagement.repositories;

import org.collegemanagement.entity.Loan;
import org.collegemanagement.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByStudentId(Long studentId);
    List<Loan> findByBookCollegeId(Long collegeId);
    List<Loan> findByBookCollegeIdAndStatus(Long collegeId, LoanStatus status);
}

