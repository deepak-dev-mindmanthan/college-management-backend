package org.collegemanagement.services;

import org.collegemanagement.entity.Fee;

import java.util.List;

public interface FeeService {
    List<Fee> findByStudentId(long id);
    Fee findById(long id);
    Fee save(Fee fee);
    double getPendingFees(long studentId);
}
