package org.collegemanagement.mapper;

import org.collegemanagement.dto.student.EnrollmentInfo;
import org.collegemanagement.dto.student.ParentInfo;
import org.collegemanagement.dto.student.StudentDetailResponse;
import org.collegemanagement.dto.student.StudentResponse;
import org.collegemanagement.entity.academic.StudentEnrollment;
import org.collegemanagement.entity.student.Parent;
import org.collegemanagement.entity.student.ParentStudent;
import org.collegemanagement.entity.student.Student;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class StudentMapper {

    private StudentMapper() {
    }

    /**
     * Convert Student entity to StudentResponse
     */
    public static StudentResponse toResponse(Student student) {
        if (student == null) {
            return null;
        }

        return StudentResponse.builder()
                .uuid(student.getUuid())
                .name(student.getUser() != null ? student.getUser().getName() : null)
                .email(student.getUser() != null ? student.getUser().getEmail() : null)
                .rollNumber(student.getRollNumber())
                .registrationNumber(student.getRegistrationNumber())
                .dob(student.getDob())
                .gender(student.getGender())
                .admissionDate(student.getAdmissionDate())
                .bloodGroup(student.getBloodGroup())
                .address(student.getAddress())
                .status(student.getStatus())
                .emailVerified(student.getUser() != null ? student.getUser().getEmailVerified() : null)
                .lastLoginAt(student.getUser() != null ? student.getUser().getLastLoginAt() : null)
                .collegeId(student.getCollege() != null ? student.getCollege().getId() : null)
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }

    /**
     * Convert Student entity to StudentDetailResponse with additional information
     */
    public static StudentDetailResponse toDetailResponse(
            Student student,
            List<ParentStudent> parentStudents,
            List<StudentEnrollment> enrollments
    ) {
        if (student == null) {
            return null;
        }

        StudentDetailResponse.StudentDetailResponseBuilder builder = StudentDetailResponse.builder()
                .uuid(student.getUuid())
                .name(student.getUser() != null ? student.getUser().getName() : null)
                .email(student.getUser() != null ? student.getUser().getEmail() : null)
                .rollNumber(student.getRollNumber())
                .registrationNumber(student.getRegistrationNumber())
                .dob(student.getDob())
                .gender(student.getGender())
                .admissionDate(student.getAdmissionDate())
                .bloodGroup(student.getBloodGroup())
                .address(student.getAddress())
                .status(student.getStatus())
                .emailVerified(student.getUser() != null ? student.getUser().getEmailVerified() : null)
                .lastLoginAt(student.getUser() != null ? student.getUser().getLastLoginAt() : null)
                .collegeId(student.getCollege() != null ? student.getCollege().getId() : null)
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt());

        // Map parents
        List<ParentInfo> parents = Collections.emptyList();
        if (parentStudents != null && !parentStudents.isEmpty()) {
            parents = parentStudents.stream()
                    .map(StudentMapper::mapParentInfo)
                    .collect(Collectors.toList());
        }
        builder.parents(parents);

        // Map enrollments
        List<EnrollmentInfo> enrollmentInfos = Collections.emptyList();
        if (enrollments != null && !enrollments.isEmpty()) {
            enrollmentInfos = enrollments.stream()
                    .map(StudentMapper::mapEnrollmentInfo)
                    .collect(Collectors.toList());
        }
        builder.enrollments(enrollmentInfos);

        // Find current active enrollment
        EnrollmentInfo currentEnrollment = null;
        if (enrollments != null && !enrollments.isEmpty()) {
            StudentEnrollment activeEnrollment = enrollments.stream()
                    .filter(e -> e.getStatus() == org.collegemanagement.enums.EnrollmentStatus.ACTIVE)
                    .findFirst()
                    .orElse(null);
            if (activeEnrollment != null) {
                currentEnrollment = mapEnrollmentInfo(activeEnrollment);
            }
        }
        builder.currentEnrollment(currentEnrollment);

        return builder.build();
    }

    /**
     * Map ParentStudent to ParentInfo
     */
    private static ParentInfo mapParentInfo(ParentStudent parentStudent) {
        if (parentStudent == null || parentStudent.getParent() == null) {
            return null;
        }

        Parent parent = parentStudent.getParent();
        return ParentInfo.builder()
                .uuid(parent.getUuid())
                .name(parent.getUser() != null ? parent.getUser().getName() : null)
                .email(parent.getUser() != null ? parent.getUser().getEmail() : null)
                .occupation(parent.getOccupation())
                .relation(parentStudent.getRelation())
                .build();
    }

    /**
     * Map StudentEnrollment to EnrollmentInfo
     */
    private static EnrollmentInfo mapEnrollmentInfo(StudentEnrollment enrollment) {
        if (enrollment == null) {
            return null;
        }

        return EnrollmentInfo.builder()
                .uuid(enrollment.getUuid())
                .academicYearUuid(enrollment.getAcademicYear() != null ? enrollment.getAcademicYear().getUuid() : null)
                .academicYearName(enrollment.getAcademicYear() != null ? enrollment.getAcademicYear().getYearName() : null)
                .classUuid(enrollment.getClassRoom() != null ? enrollment.getClassRoom().getUuid() : null)
                .className(enrollment.getClassRoom() != null ? enrollment.getClassRoom().getName() : null)
                .section(enrollment.getClassRoom() != null ? enrollment.getClassRoom().getSection() : null)
                .rollNumber(enrollment.getRollNumber())
                .status(enrollment.getStatus())
                .createdAt(enrollment.getCreatedAt())
                .build();
    }
}

