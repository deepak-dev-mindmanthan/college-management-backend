package org.collegemanagement.services;

import org.collegemanagement.entity.College;
import org.collegemanagement.dto.CollegeDto;

import java.util.List;

public interface CollegeService {
    boolean existsByName(String collegeName);
    CollegeDto create(CollegeDto collegeDto);
    College findByName(String name);
    College findByEmail(String email);
    College findById(long id);
    List<CollegeDto> findAll();
    void deleteCollege(Long id);
    boolean existsById(Long id);
    boolean exitsByPhone(String phone);
    long count();

}
