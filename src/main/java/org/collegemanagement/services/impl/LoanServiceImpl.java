package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import org.collegemanagement.entity.Loan;
import org.collegemanagement.enums.LoanStatus;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.repositories.LoanRepository;
import org.collegemanagement.services.LoanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;

    @Transactional
    @Override
    public Loan create(Loan loan) {
        return loanRepository.save(loan);
    }

    @Transactional
    @Override
    public Loan update(Loan loan) {
        return loanRepository.save(loan);
    }

    @Override
    public Loan findById(Long id) {
        return loanRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));
    }

    @Override
    public List<Loan> findByStudent(Long studentId) {
        return loanRepository.findByStudentId(studentId);
    }

    @Override
    public List<Loan> findByCollege(Long collegeId) {
        return loanRepository.findByBookCollegeId(collegeId);
    }

    @Override
    public List<Loan> findByCollegeAndStatus(Long collegeId, LoanStatus status) {
        return loanRepository.findByBookCollegeIdAndStatus(collegeId, status);
    }
}

