package org.collegemanagement.services;

import org.collegemanagement.entity.Loan;
import org.collegemanagement.enums.LoanStatus;

import java.util.List;

public interface LoanService {
    Loan create(Loan loan);
    Loan update(Loan loan);
    Loan findById(Long id);
    List<Loan> findByStudent(Long studentId);
    List<Loan> findByCollege(Long collegeId);
    List<Loan> findByCollegeAndStatus(Long collegeId, LoanStatus status);
}

