package org.collegemanagement.services.impl;

import lombok.AllArgsConstructor;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.dto.CollegeDto;
import org.collegemanagement.mapper.CollegeMapper;
import org.collegemanagement.repositories.CollegeRepository;
import org.collegemanagement.services.CollegeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CollegeServiceImpl implements CollegeService {

    private final CollegeRepository collegeRepository;

    @Override
    public boolean existsByName(String collegeName) {
        return collegeRepository.existsByName(collegeName);
    }


    @Transactional
    @Override
    public CollegeDto create(College college) {
        return CollegeMapper.toDto(collegeRepository.save(college));
    }

    @Override
    public College findByName(String name) {
        return collegeRepository.findCollegeByName(name);
    }

    @Override
    public College findByEmail(String email) {
        return collegeRepository.findCollegeByEmail(email);
    }

    @Override
    public College findById(long id) {
        return collegeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("College not found with id: " + id));
    }

    @Override
    public List<CollegeDto> findAll() {
        return collegeRepository.findAll().stream().map(CollegeMapper::toDto).collect(Collectors.toList());
    }


    @Transactional
    @Override
    public void deleteCollege(Long id) {
        Optional<College> collegeOptional = collegeRepository.findById(id);
        if(collegeOptional.isPresent()){
            collegeRepository.delete(collegeOptional.get());
        }else{
            throw new ResourceNotFoundException("College not found with id: " + id);
        }
    }

    @Override
    public boolean existsById(Long id) {
        return collegeRepository.existsById(id);
    }

    @Override
    public boolean exitsByPhone(String phone) {
        return collegeRepository.existsCollegeByPhone(phone);
    }

    @Override
    public long count() {
        return collegeRepository.count();
    }

    @Override
    public College findByUuid(String uuid) {
        College college = collegeRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("College not found"));;
        return college;
    }

    @Override
    public boolean existsByUuid(String uuid) {
        return collegeRepository.existsByUuid(uuid);
    }

    @Override
    public boolean existsByEmail(String email) {
        return collegeRepository.existsCollegeByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return collegeRepository.existsCollegeByPhone(phone);
    }

    @Override
    public boolean exitsByShortCode(String shortCode) {
        return collegeRepository.existsCollegeByShortCode(shortCode);
    }
}
