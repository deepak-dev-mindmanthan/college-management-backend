package org.collegemanagement.services.impl;

import lombok.AllArgsConstructor;
import org.collegemanagement.entity.College;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.dto.CollegeDto;
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
    public CollegeDto create(CollegeDto collegeDto) {
        College college = CollegeDto.toEntity(collegeDto);
        return CollegeDto.fromEntity(collegeRepository.save(college));
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
        return collegeRepository.findAll().stream().map(CollegeDto::fromEntity).collect(Collectors.toList());
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
    public long count() {
        return collegeRepository.count();
    }
}
