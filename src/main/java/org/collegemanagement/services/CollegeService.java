package org.collegemanagement.services;

import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.dto.CollegeDto;

import java.util.List;

public interface CollegeService {
    boolean existsByName(String collegeName);
    CollegeDto create(College college);
    College findByName(String name);
    College findByEmail(String email);
    College findById(long id);
    List<CollegeDto> findAll();
    void deleteCollege(Long id);
    boolean existsById(Long id);
    boolean exitsByPhone(String phone);
    long count();
    College findByUuid(String uuid);
    boolean existsByUuid(String uuid);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean exitsByShortCode(String shortCode);


}
