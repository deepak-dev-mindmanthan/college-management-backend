package org.collegemanagement.services;

import org.collegemanagement.entity.Fee;

import java.util.List;

public interface FeeService {
    List<Fee> findByStudentId(long id);
    List<Fee> findByCollegeId(long collegeId);
    List<Fee> findByCollegeIdAndStatus(long collegeId, org.collegemanagement.enums.FeeStatus status);
    Fee findById(long id);
    Fee save(Fee fee);
    double getPendingFees(long studentId);
}
