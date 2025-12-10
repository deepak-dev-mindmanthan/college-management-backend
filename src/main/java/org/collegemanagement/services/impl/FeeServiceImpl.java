package org.collegemanagement.services.impl;

import org.collegemanagement.entity.Fee;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.repositories.FeeRepository;
import org.collegemanagement.services.FeeService;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class FeeServiceImpl implements FeeService {
    private final FeeRepository feeRepository;

    public FeeServiceImpl(FeeRepository feeRepository) {
        this.feeRepository = feeRepository;
    }

    @Override
    public List<Fee> findByStudentId(long id) {
        return feeRepository.findByStudentId(id);
    }

    @Override
    public List<Fee> findByCollegeId(long collegeId) {
        return feeRepository.findByStudentCollegeId(collegeId);
    }

    @Override
    public List<Fee> findByCollegeIdAndStatus(long collegeId, org.collegemanagement.enums.FeeStatus status) {
        return feeRepository.findByStudentCollegeIdAndStatus(collegeId, status);
    }

    @Override
    public Fee findById(long id) {
        return feeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Fee not found with id:"+id));
    }

    @Transactional
    @Override
    public Fee save(Fee fee) {
        return feeRepository.save(fee);
    }

    @Override
    public double getPendingFees(long studentId) {
        return feeRepository.getPendingFees(studentId);
    }
}
