package org.collegemanagement.mapper;

import org.collegemanagement.dto.AcademicYearDto;
import org.collegemanagement.dto.CollegeDto;
import org.collegemanagement.dto.DepartmentDto;
import org.collegemanagement.entity.tenant.College;

import java.util.Collections;
import java.util.stream.Collectors;

public final class CollegeMapper {

    private CollegeMapper() {}

    public static CollegeDto toDto(College college) {

        if (college == null) return null;

        return CollegeDto.builder()
                .id(college.getId())
                .uuid(college.getUuid())
                .name(college.getName())
                .shortCode(college.getShortCode())
                .logoUrl(college.getLogoUrl())
                .email(college.getEmail())
                .phone(college.getPhone())
                .country(college.getCountry())
                .state(college.getState())
                .city(college.getCity())
                .address(college.getAddress())
                .status(college.getStatus())
                .departments(
                        college.getDepartments() == null
                                ? Collections.emptyList()
                                : college.getDepartments().stream()
                                .map(d -> DepartmentDto.builder()
                                        .uuid(d.getUuid())
                                        .name(d.getName())
                                        .code(d.getCode())
                                        .headUserUuid(
                                                d.getHead() != null
                                                        ? d.getHead().getUuid()
                                                        : null
                                        )
                                        .headUserName(
                                                d.getHead() != null
                                                        ? d.getHead().getName()
                                                        : null
                                        )
                                        .build()
                                )
                                .collect(Collectors.toList())
                )
                .academicYears(
                        college.getAcademicYears() == null
                                ? Collections.emptyList()
                                : college.getAcademicYears().stream()
                                .map(y -> AcademicYearDto.builder()
                                        .uuid(y.getUuid())
                                        .yearName(y.getYearName())
                                        .startDate(y.getStartDate())
                                        .endDate(y.getEndDate())
                                        .active(y.getActive())
                                        .build()
                                )
                                .collect(Collectors.toList())
                )
                .createdAt(college.getCreatedAt())
                .updatedAt(college.getUpdatedAt())
                .build();
    }
}
